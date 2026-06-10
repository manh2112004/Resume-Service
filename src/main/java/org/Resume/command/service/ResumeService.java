package org.Resume.command.service;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface ResumeService {
    CompletableFuture<String> createResume(String candidateId, MultipartFile file, Boolean isDefault);
    CompletableFuture<Void> setDefaultResume(String candidateId, String resumeId);
    CompletableFuture<Void> deleteResume(String candidateId, String resumeId);
    CompletableFuture<String> addSkill(String candidateId, String resumeId, String skillName, String level);
    CompletableFuture<Void> updateSkill(String candidateId, String resumeId, String skillId, String skillName, String level);
    CompletableFuture<Void> deleteSkill(String candidateId, String resumeId, String skillId);

    CompletableFuture<String> addEducation(String candidateId, String resumeId, String schoolName, String major, String degree, String startDate, String endDate, String description);
    CompletableFuture<Void> updateEducation(String candidateId, String resumeId, String educationId, String schoolName, String major, String degree, String startDate, String endDate, String description);
    CompletableFuture<Void> deleteEducation(String candidateId, String resumeId, String educationId);

    CompletableFuture<String> addExperience(String candidateId, String resumeId, String companyName, String position, LocalDate startDate, LocalDate endDate, Boolean currentJob, String description);
    CompletableFuture<Void> updateExperience(String candidateId, String resumeId, String experienceId, String companyName, String position, LocalDate startDate, LocalDate endDate, Boolean currentJob, String description);
    CompletableFuture<Void> deleteExperience(String candidateId, String resumeId, String experienceId);

    CompletableFuture<String> addProject(String candidateId, String resumeId, String projectName, String role, String description, String technologies, String projectUrl);
    CompletableFuture<Void> updateProject(String candidateId, String resumeId, String projectId, String projectName, String role, String description, String technologies, String projectUrl);
    CompletableFuture<Void> deleteProject(String candidateId, String resumeId, String projectId);

    CompletableFuture<Void> parseResume(String candidateId, String resumeId);
}
