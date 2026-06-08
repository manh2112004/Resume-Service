package org.Resume.command.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.Resume.command.command.CreateResumeCommand;
import org.Resume.command.command.SetDefaultResumeCommand;
import org.Resume.command.command.DeleteResumeCommand;
import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.Resume.command.service.ResumeService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public CompletableFuture<String> createResume(String candidateId, MultipartFile file, Boolean isDefault) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng từ token");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn file CV");
        }

        validateResumeFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "resume_" + UUID.randomUUID().toString();
        }

        String extension = getFileExtension(originalFilename);
        
        // Upload to Cloudinary
        String fileUrl = uploadFileToCloudinary(file, "resume-service/resumes", extension);
        long fileSize = file.getSize();

        // Check if this is the first resume of the candidate
        boolean hasResumes = resumeRepository.existsByCandidateId(candidateId);
        boolean finalIsDefault = !hasResumes || (isDefault != null && isDefault);

        CreateResumeCommand command = CreateResumeCommand.builder()
                .id(UUID.randomUUID().toString())
                .candidateId(candidateId)
                .fileName(originalFilename)
                .fileUrl(fileUrl)
                .fileType(extension != null ? extension.toLowerCase() : "pdf")
                .fileSize(fileSize)
                .isDefault(finalIsDefault)
                .build();

        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<Void> setDefaultResume(String candidateId, String resumeId) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng từ token");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV"));

        if (resume.getStatus() == org.Resume.constant.ResumeStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV");
        }

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa CV này");
        }

        SetDefaultResumeCommand command = SetDefaultResumeCommand.builder()
                .id(resumeId)
                .candidateId(candidateId)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteResume(String candidateId, String resumeId) {
        if (candidateId == null || candidateId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng từ token");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV"));

        if (resume.getStatus() == org.Resume.constant.ResumeStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV");
        }

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa CV này");
        }

        DeleteResumeCommand command = DeleteResumeCommand.builder()
                .id(resumeId)
                .candidateId(candidateId)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    private void validateResumeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên file không hợp lệ");
        }

        String extension = getFileExtension(originalFilename);
        if (extension == null || 
            (!extension.equalsIgnoreCase("pdf") && 
             !extension.equalsIgnoreCase("doc") && 
             !extension.equalsIgnoreCase("docx"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận các định dạng file: PDF, DOC, DOCX");
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return null;
    }

    private String uploadFileToCloudinary(MultipartFile file, String folder, String extension) {
        try {
            String publicId = UUID.randomUUID().toString();
            if (extension != null && !extension.isBlank()) {
                publicId = publicId + "." + extension.toLowerCase();
            }

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "resource_type", "raw"
            ));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary không trả về URL file");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload CV thất bại", e);
        }
    }
}
