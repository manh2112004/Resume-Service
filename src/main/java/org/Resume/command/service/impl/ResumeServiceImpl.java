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

    @Autowired
    private org.Resume.command.data.ResumeSkillRepository resumeSkillRepository;

    @Autowired
    private org.Resume.command.data.ResumeEducationRepository resumeEducationRepository;

    @Override
    public CompletableFuture<String> addSkill(String candidateId, String resumeId, String skillName, String level) {
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

        if (skillName == null || skillName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên kỹ năng không được để trống");
        }

        String skillId = UUID.randomUUID().toString();

        org.Resume.command.command.AddResumeSkillCommand command = org.Resume.command.command.AddResumeSkillCommand.builder()
                .resumeId(resumeId)
                .skillId(skillId)
                .skillName(skillName)
                .level(level)
                .build();

        return commandGateway.send(command).thenApply(result -> skillId);
    }

    @Override
    public CompletableFuture<Void> updateSkill(String candidateId, String resumeId, String skillId, String skillName, String level) {
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

        org.Resume.command.data.ResumeSkill skill = resumeSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kỹ năng"));

        if (!skill.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kỹ năng không thuộc về CV này");
        }

        String finalSkillName = (skillName == null || skillName.isBlank()) ? skill.getSkillName() : skillName;
        String finalLevel = (level == null || level.isBlank()) ? skill.getLevel() : level;

        org.Resume.command.command.UpdateResumeSkillCommand command = org.Resume.command.command.UpdateResumeSkillCommand.builder()
                .resumeId(resumeId)
                .skillId(skillId)
                .skillName(finalSkillName)
                .level(finalLevel)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteSkill(String candidateId, String resumeId, String skillId) {
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

        org.Resume.command.data.ResumeSkill skill = resumeSkillRepository.findById(skillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy kỹ năng"));

        if (!skill.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kỹ năng không thuộc về CV này");
        }

        org.Resume.command.command.DeleteResumeSkillCommand command = org.Resume.command.command.DeleteResumeSkillCommand.builder()
                .resumeId(resumeId)
                .skillId(skillId)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<String> addEducation(String candidateId, String resumeId, String schoolName, String major, String degree, String startDate, String endDate, String description) {
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

        if (schoolName == null || schoolName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên trường không được để trống");
        }

        String educationId = UUID.randomUUID().toString();

        org.Resume.command.command.AddResumeEducationCommand command = org.Resume.command.command.AddResumeEducationCommand.builder()
                .resumeId(resumeId)
                .educationId(educationId)
                .schoolName(schoolName)
                .major(major)
                .degree(degree)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
                .build();

        return commandGateway.send(command).thenApply(result -> educationId);
    }

    @Override
    public CompletableFuture<Void> updateEducation(String candidateId, String resumeId, String educationId, String schoolName, String major, String degree, String startDate, String endDate, String description) {
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

        org.Resume.command.data.ResumeEducation education = resumeEducationRepository.findById(educationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin học vấn"));

        if (!education.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin học vấn không thuộc về CV này");
        }

        String finalSchoolName = (schoolName == null || schoolName.isBlank()) ? education.getSchoolName() : schoolName;
        String finalMajor = (major == null || major.isBlank()) ? education.getMajor() : major;
        String finalDegree = (degree == null || degree.isBlank()) ? education.getDegree() : degree;
        String finalStartDate = (startDate == null || startDate.isBlank()) ? education.getStartDate() : startDate;
        String finalEndDate = (endDate == null || endDate.isBlank()) ? education.getEndDate() : endDate;
        String finalDescription = (description == null || description.isBlank()) ? education.getDescription() : description;

        org.Resume.command.command.UpdateResumeEducationCommand command = org.Resume.command.command.UpdateResumeEducationCommand.builder()
                .resumeId(resumeId)
                .educationId(educationId)
                .schoolName(finalSchoolName)
                .major(finalMajor)
                .degree(finalDegree)
                .startDate(finalStartDate)
                .endDate(finalEndDate)
                .description(finalDescription)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteEducation(String candidateId, String resumeId, String educationId) {
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

        org.Resume.command.data.ResumeEducation education = resumeEducationRepository.findById(educationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin học vấn"));

        if (!education.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin học vấn không thuộc về CV này");
        }

        org.Resume.command.command.DeleteResumeEducationCommand command = org.Resume.command.command.DeleteResumeEducationCommand.builder()
                .resumeId(resumeId)
                .educationId(educationId)
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
