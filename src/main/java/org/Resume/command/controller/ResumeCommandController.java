package org.Resume.command.controller;

import org.Resume.command.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeCommandController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<String> createResume(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDefault", required = false) Boolean isDefault
    ) {
        return resumeService.createResume(jwt.getSubject(), file, isDefault);
    }

    @PutMapping("/{resumeId}/default")
    public CompletableFuture<Void> setDefaultResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId
    ) {
        return resumeService.setDefaultResume(jwt.getSubject(), resumeId);
    }

    @DeleteMapping("/{resumeId}")
    public CompletableFuture<Void> deleteResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId
    ) {
        return resumeService.deleteResume(jwt.getSubject(), resumeId);
    }

    @PostMapping("/{resumeId}/skills")
    public CompletableFuture<String> addSkill(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @RequestBody SkillRequest request
    ) {
        return resumeService.addSkill(jwt.getSubject(), resumeId, request.getSkillName(), request.getLevel());
    }

    @PutMapping("/{resumeId}/skills/{skillId}")
    public CompletableFuture<Void> updateSkill(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @PathVariable String skillId,
            @RequestBody SkillRequest request
    ) {
        return resumeService.updateSkill(jwt.getSubject(), resumeId, skillId, request.getSkillName(), request.getLevel());
    }

    @DeleteMapping("/{resumeId}/skills/{skillId}")
    public CompletableFuture<Void> deleteSkill(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @PathVariable String skillId
    ) {
        return resumeService.deleteSkill(jwt.getSubject(), resumeId, skillId);
    }

    @PostMapping("/{resumeId}/educations")
    public CompletableFuture<String> addEducation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @RequestBody EducationRequest request
    ) {
        return resumeService.addEducation(jwt.getSubject(), resumeId, request.getSchoolName(), request.getMajor(), request.getDegree(), request.getStartDate(), request.getEndDate(), request.getDescription());
    }

    @PutMapping("/{resumeId}/educations/{educationId}")
    public CompletableFuture<Void> updateEducation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @PathVariable String educationId,
            @RequestBody EducationRequest request
    ) {
        return resumeService.updateEducation(jwt.getSubject(), resumeId, educationId, request.getSchoolName(), request.getMajor(), request.getDegree(), request.getStartDate(), request.getEndDate(), request.getDescription());
    }

    @DeleteMapping("/{resumeId}/educations/{educationId}")
    public CompletableFuture<Void> deleteEducation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId,
            @PathVariable String educationId
    ) {
        return resumeService.deleteEducation(jwt.getSubject(), resumeId, educationId);
    }

    @lombok.Data
    public static class SkillRequest {
        private String skillName;
        private String level;
    }

    @lombok.Data
    public static class EducationRequest {
        private String schoolName;
        private String major;
        private String degree;
        private String startDate;
        private String endDate;
        private String description;
    }
}
