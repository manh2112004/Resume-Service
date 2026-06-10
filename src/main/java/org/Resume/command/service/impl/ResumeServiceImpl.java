package org.Resume.command.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.Resume.command.command.*;
import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.Resume.command.service.ResumeService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;
import org.apache.tika.Tika;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private org.Resume.command.data.ResumeExperienceRepository resumeExperienceRepository;

    @Autowired
    private org.Resume.command.data.ResumeProjectRepository resumeProjectRepository;

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

    @Override
    public CompletableFuture<String> addExperience(String candidateId, String resumeId, String companyName, String position, LocalDate startDate, LocalDate endDate, Boolean currentJob, String description) {
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

        if (companyName == null || companyName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên công ty không được để trống");
        }

        String experienceId = UUID.randomUUID().toString();

        org.Resume.command.command.AddResumeExperienceCommand command = org.Resume.command.command.AddResumeExperienceCommand.builder()
                .resumeId(resumeId)
                .experienceId(experienceId)
                .companyName(companyName)
                .position(position)
                .startDate(startDate)
                .endDate(endDate)
                .currentJob(currentJob)
                .description(description)
                .build();

        return commandGateway.send(command).thenApply(result -> experienceId);
    }

    @Override
    public CompletableFuture<Void> updateExperience(String candidateId, String resumeId, String experienceId, String companyName, String position, LocalDate startDate, LocalDate endDate, Boolean currentJob, String description) {
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

        org.Resume.command.data.ResumeExperience experience = resumeExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin kinh nghiệm"));

        if (!experience.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin kinh nghiệm không thuộc về CV này");
        }

        String finalCompanyName = (companyName == null || companyName.isBlank()) ? experience.getCompanyName() : companyName;
        String finalPosition = (position == null || position.isBlank()) ? experience.getPosition() : position;
        LocalDate finalStartDate = (startDate == null) ? experience.getStartDate() : startDate;
        LocalDate finalEndDate = (endDate == null) ? experience.getEndDate() : endDate;
        Boolean finalCurrentJob = (currentJob == null) ? experience.getCurrentJob() : currentJob;
        String finalDescription = (description == null || description.isBlank()) ? experience.getDescription() : description;

        org.Resume.command.command.UpdateResumeExperienceCommand command = org.Resume.command.command.UpdateResumeExperienceCommand.builder()
                .resumeId(resumeId)
                .experienceId(experienceId)
                .companyName(finalCompanyName)
                .position(finalPosition)
                .startDate(finalStartDate)
                .endDate(finalEndDate)
                .currentJob(finalCurrentJob)
                .description(finalDescription)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteExperience(String candidateId, String resumeId, String experienceId) {
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

        org.Resume.command.data.ResumeExperience experience = resumeExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin kinh nghiệm"));

        if (!experience.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin kinh nghiệm không thuộc về CV này");
        }

        org.Resume.command.command.DeleteResumeExperienceCommand command = org.Resume.command.command.DeleteResumeExperienceCommand.builder()
                .resumeId(resumeId)
                .experienceId(experienceId)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<String> addProject(String candidateId, String resumeId, String projectName, String role, String description, String technologies, String projectUrl) {
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

        if (projectName == null || projectName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên dự án không được để trống");
        }

        String projectId = UUID.randomUUID().toString();

        org.Resume.command.command.AddResumeProjectCommand command = org.Resume.command.command.AddResumeProjectCommand.builder()
                .resumeId(resumeId)
                .projectId(projectId)
                .projectName(projectName)
                .role(role)
                .description(description)
                .technologies(technologies)
                .projectUrl(projectUrl)
                .build();

        return commandGateway.send(command).thenApply(result -> projectId);
    }

    @Override
    public CompletableFuture<Void> updateProject(String candidateId, String resumeId, String projectId, String projectName, String role, String description, String technologies, String projectUrl) {
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

        org.Resume.command.data.ResumeProject project = resumeProjectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin dự án"));

        if (!project.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin dự án không thuộc về CV này");
        }

        String finalProjectName = (projectName == null || projectName.isBlank()) ? project.getProjectName() : projectName;
        String finalRole = (role == null || role.isBlank()) ? project.getRole() : role;
        String finalDescription = (description == null || description.isBlank()) ? project.getDescription() : description;
        String finalTechnologies = (technologies == null || technologies.isBlank()) ? project.getTechnologies() : technologies;
        String finalProjectUrl = (projectUrl == null || projectUrl.isBlank()) ? project.getProjectUrl() : projectUrl;

        org.Resume.command.command.UpdateResumeProjectCommand command = org.Resume.command.command.UpdateResumeProjectCommand.builder()
                .resumeId(resumeId)
                .projectId(projectId)
                .projectName(finalProjectName)
                .role(finalRole)
                .description(finalDescription)
                .technologies(finalTechnologies)
                .projectUrl(finalProjectUrl)
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteProject(String candidateId, String resumeId, String projectId) {
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

        org.Resume.command.data.ResumeProject project = resumeProjectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin dự án"));

        if (!project.getResume().getId().equals(resumeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin dự án không thuộc về CV này");
        }

        org.Resume.command.command.DeleteResumeProjectCommand command = org.Resume.command.command.DeleteResumeProjectCommand.builder()
                .resumeId(resumeId)
                .projectId(projectId)
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

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Override
    public CompletableFuture<Void> parseResume(String candidateId, String resumeId) {
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

        return CompletableFuture.runAsync(() -> {
            try {
                // 1. Download file bytes
                byte[] fileBytes = downloadFile(resume.getFileUrl());

                // 2. Extract raw text using Tika
                Tika tika = new Tika();
                String rawText = tika.parseToString(new java.io.ByteArrayInputStream(fileBytes));
                if (rawText == null || rawText.isBlank()) {
                    rawText = "Empty resume text";
                }

                // 3. Extract structured data using Gemini API
                String promptText = "You are a professional resume parser. Extract the candidate information from the resume raw text.\n" +
                        "Return the output ONLY as a valid JSON object matching the JSON Schema. Do not output any markdown code blocks, backticks, or extra text.\n" +
                        "JSON Schema:\n" +
                        "{\n" +
                        "  \"fullName\": \"string or null\",\n" +
                        "  \"email\": \"string or null\",\n" +
                        "  \"phone\": \"string or null\",\n" +
                        "  \"address\": \"string or null\",\n" +
                        "  \"summary\": \"string or null\",\n" +
                        "  \"totalExperienceYears\": integer,\n" +
                        "  \"skills\": [\n" +
                        "    {\n" +
                        "      \"skillName\": \"string\",\n" +
                        "      \"level\": \"string or null\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"educations\": [\n" +
                        "    {\n" +
                        "      \"schoolName\": \"string\",\n" +
                        "      \"major\": \"string or null\",\n" +
                        "      \"degree\": \"string or null\",\n" +
                        "      \"startDate\": \"string or null (e.g. Month Year or Year)\",\n" +
                        "      \"endDate\": \"string or null (e.g. Month Year or Year)\",\n" +
                        "      \"description\": \"string or null\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"experiences\": [\n" +
                        "    {\n" +
                        "      \"companyName\": \"string\",\n" +
                        "      \"position\": \"string or null\",\n" +
                        "      \"startDate\": \"yyyy-MM-dd or null\",\n" +
                        "      \"endDate\": \"yyyy-MM-dd or null\",\n" +
                        "      \"currentJob\": boolean,\n" +
                        "      \"description\": \"string or null\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"projects\": [\n" +
                        "    {\n" +
                        "      \"projectName\": \"string\",\n" +
                        "      \"role\": \"string or null\",\n" +
                        "      \"description\": \"string or null\",\n" +
                        "      \"technologies\": \"string or null (comma separated list of technologies)\",\n" +
                        "      \"projectUrl\": \"string or null\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Resume text:\n" +
                        rawText;

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                List<Map<String, Object>> contents = new ArrayList<>();
                Map<String, Object> contentMap = new HashMap<>();
                List<Map<String, Object>> parts = new ArrayList<>();
                Map<String, Object> partMap = new HashMap<>();
                partMap.put("text", promptText);
                parts.add(partMap);
                contentMap.put("parts", parts);
                contents.add(contentMap);
                requestBody.put("contents", contents);

                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                requestBody.put("generationConfig", generationConfig);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                Map responseBody = response.getBody();
                if (responseBody == null || !responseBody.containsKey("candidates")) {
                    throw new RuntimeException("Gemini API did not return candidates");
                }
                List candidates = (List) responseBody.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    throw new RuntimeException("Gemini API candidates list is empty");
                }
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                List partsList = (List) content.get("parts");
                Map part = (Map) partsList.get(0);
                String jsonText = (String) part.get("text");

                // 4. Parse JSON Response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonText);
                String fullName = rootNode.path("fullName").isTextual() ? rootNode.path("fullName").asText(null) : null;
                String email = rootNode.path("email").isTextual() ? rootNode.path("email").asText(null) : null;
                String phone = rootNode.path("phone").isTextual() ? rootNode.path("phone").asText(null) : null;
                String address = rootNode.path("address").isTextual() ? rootNode.path("address").asText(null) : null;
                String summary = rootNode.path("summary").isTextual() ? rootNode.path("summary").asText(null) : null;
                int totalExperienceYears = rootNode.path("totalExperienceYears").isInt() ? rootNode.path("totalExperienceYears").asInt(0) : 0;

                // 5. Dispatch Axon Command
                ParseResumeCommand command = ParseResumeCommand.builder()
                        .id(resumeId)
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .address(address)
                        .summary(summary)
                        .totalExperienceYears(totalExperienceYears)
                        .rawText(rawText)
                        .build();

                commandGateway.sendAndWait(command);

                // 6. Parse and Dispatch Skills Commands
                if (rootNode.has("skills") && rootNode.get("skills").isArray()) {
                    for (JsonNode skillNode : rootNode.get("skills")) {
                        String skillName = skillNode.path("skillName").asText(null);
                        if (skillName != null && !skillName.isBlank()) {
                            String level = skillNode.path("level").asText(null);
                            commandGateway.send(AddResumeSkillCommand.builder()
                                    .resumeId(resumeId)
                                    .skillId(UUID.randomUUID().toString())
                                    .skillName(skillName)
                                    .level(level)
                                    .build());
                        }
                    }
                }

                // 7. Parse and Dispatch Educations Commands
                if (rootNode.has("educations") && rootNode.get("educations").isArray()) {
                    for (JsonNode eduNode : rootNode.get("educations")) {
                        String schoolName = eduNode.path("schoolName").asText(null);
                        if (schoolName != null && !schoolName.isBlank()) {
                            commandGateway.send(AddResumeEducationCommand.builder()
                                    .resumeId(resumeId)
                                    .educationId(UUID.randomUUID().toString())
                                    .schoolName(schoolName)
                                    .major(eduNode.path("major").isTextual() ? eduNode.path("major").asText(null) : null)
                                    .degree(eduNode.path("degree").isTextual() ? eduNode.path("degree").asText(null) : null)
                                    .startDate(eduNode.path("startDate").isTextual() ? eduNode.path("startDate").asText(null) : null)
                                    .endDate(eduNode.path("endDate").isTextual() ? eduNode.path("endDate").asText(null) : null)
                                    .description(eduNode.path("description").isTextual() ? eduNode.path("description").asText(null) : null)
                                    .build());
                        }
                    }
                }

                // 8. Parse and Dispatch Experiences Commands
                if (rootNode.has("experiences") && rootNode.get("experiences").isArray()) {
                    for (JsonNode expNode : rootNode.get("experiences")) {
                        String companyName = expNode.path("companyName").asText(null);
                        if (companyName != null && !companyName.isBlank()) {
                            LocalDate startDate = null;
                            LocalDate endDate = null;
                            try {
                                String sd = expNode.path("startDate").asText(null);
                                if (sd != null && !sd.isBlank()) {
                                    startDate = LocalDate.parse(sd);
                                }
                            } catch (Exception ex) {}
                            try {
                                String ed = expNode.path("endDate").asText(null);
                                if (ed != null && !ed.isBlank()) {
                                    endDate = LocalDate.parse(ed);
                                }
                            } catch (Exception ex) {}

                            commandGateway.send(AddResumeExperienceCommand.builder()
                                    .resumeId(resumeId)
                                    .experienceId(UUID.randomUUID().toString())
                                    .companyName(companyName)
                                    .position(expNode.path("position").isTextual() ? expNode.path("position").asText(null) : null)
                                    .startDate(startDate)
                                    .endDate(endDate)
                                    .currentJob(expNode.path("currentJob").asBoolean(false))
                                    .description(expNode.path("description").isTextual() ? expNode.path("description").asText(null) : null)
                                    .build());
                        }
                    }
                }

                // 9. Parse and Dispatch Projects Commands
                if (rootNode.has("projects") && rootNode.get("projects").isArray()) {
                    for (JsonNode projNode : rootNode.get("projects")) {
                        String projectName = projNode.path("projectName").asText(null);
                        if (projectName != null && !projectName.isBlank()) {
                            commandGateway.send(AddResumeProjectCommand.builder()
                                    .resumeId(resumeId)
                                    .projectId(UUID.randomUUID().toString())
                                    .projectName(projectName)
                                    .role(projNode.path("role").isTextual() ? projNode.path("role").asText(null) : null)
                                    .description(projNode.path("description").isTextual() ? projNode.path("description").asText(null) : null)
                                    .technologies(projNode.path("technologies").isTextual() ? projNode.path("technologies").asText(null) : null)
                                    .projectUrl(projNode.path("projectUrl").isTextual() ? projNode.path("projectUrl").asText(null) : null)
                                    .build());
                        }
                    }
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi phân tích CV: " + e.getMessage(), e);
            }
        });
    }

    private byte[] downloadFile(String fileUrl) throws Exception {
        java.net.URL url = new java.net.URL(fileUrl);
        java.net.URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        try (java.io.InputStream in = connection.getInputStream();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }
}
