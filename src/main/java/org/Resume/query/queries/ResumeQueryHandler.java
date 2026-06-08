package org.Resume.query.queries;

import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.Resume.command.data.ResumeSkill;
import org.Resume.command.data.ResumeSkillRepository;
import org.Resume.command.data.ResumeEducation;
import org.Resume.command.data.ResumeEducationRepository;
import org.Resume.command.data.ResumeExperience;
import org.Resume.command.data.ResumeExperienceRepository;
import org.Resume.command.data.ResumeProject;
import org.Resume.command.data.ResumeProjectRepository;
import org.Resume.command.data.ResumeParsedData;
import org.Resume.command.data.ResumeParsedDataRepository;
import org.Resume.constant.ResumeStatus;
import org.Resume.query.model.response.ResumeResponse;
import org.Resume.query.model.response.ResumeSkillResponse;
import org.Resume.query.model.response.ResumeEducationResponse;
import org.Resume.query.model.response.ResumeExperienceResponse;
import org.Resume.query.model.response.ResumeProjectResponse;
import org.Resume.query.model.response.ResumeIndexDataResponse;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResumeQueryHandler {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeSkillRepository resumeSkillRepository;

    @Autowired
    private ResumeEducationRepository resumeEducationRepository;

    @Autowired
    private ResumeExperienceRepository resumeExperienceRepository;

    @Autowired
    private ResumeProjectRepository resumeProjectRepository;

    @Autowired
    private ResumeParsedDataRepository resumeParsedDataRepository;

    @QueryHandler
    @Transactional(readOnly = true)
    public ResumeIndexDataResponse handle(GetResumeIndexDataQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }

        List<String> skills = resumeSkillRepository.findAllByResumeId(query.getResumeId())
                .stream()
                .map(ResumeSkill::getSkillName)
                .collect(Collectors.toList());

        List<ResumeEducationResponse> education = resumeEducationRepository.findAllByResumeId(query.getResumeId())
                .stream()
                .map(this::mapToEducationResponse)
                .collect(Collectors.toList());

        List<ResumeExperienceResponse> experience = resumeExperienceRepository.findAllByResumeId(query.getResumeId())
                .stream()
                .map(this::mapToExperienceResponse)
                .collect(Collectors.toList());

        List<ResumeProjectResponse> projects = resumeProjectRepository.findAllByResumeId(query.getResumeId())
                .stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());

        String rawText = resumeParsedDataRepository.findByResumeId(query.getResumeId())
                .map(ResumeParsedData::getRawText)
                .orElse("");

        return ResumeIndexDataResponse.builder()
                .resumeId(resume.getId())
                .candidateId(resume.getCandidateId())
                .skills(skills)
                .education(education)
                .experience(experience)
                .projects(projects)
                .rawText(rawText)
                .build();
    }


    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeResponse> handle(GetMyResumesQuery query) {
        List<Resume> resumes = resumeRepository.findAllByCandidateId(query.getCandidateId());
        return resumes.stream()
                .filter(r -> r.getStatus() != ResumeStatus.DELETED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Transactional(readOnly = true)
    public ResumeResponse handle(GetResumeByIdQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }
        return mapToResponse(resume);
    }

    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeSkillResponse> handle(GetResumeSkillsQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }

        List<ResumeSkill> skills = resumeSkillRepository.findAllByResumeId(query.getResumeId());
        return skills.stream()
                .map(this::mapToSkillResponse)
                .collect(Collectors.toList());
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .candidateId(resume.getCandidateId())
                .fileName(resume.getFileName())
                .fileUrl(resume.getFileUrl())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .status(resume.getStatus())
                .isDefault(resume.getIsDefault())
                .uploadedAt(resume.getUploadedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }

    private ResumeSkillResponse mapToSkillResponse(ResumeSkill skill) {
        return ResumeSkillResponse.builder()
                .id(skill.getId())
                .resumeId(skill.getResume().getId())
                .skillName(skill.getSkillName())
                .level(skill.getLevel())
                .build();
    }

    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeEducationResponse> handle(GetResumeEducationsQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }

        List<ResumeEducation> educations = resumeEducationRepository.findAllByResumeId(query.getResumeId());
        return educations.stream()
                .map(this::mapToEducationResponse)
                .collect(Collectors.toList());
    }

    private ResumeEducationResponse mapToEducationResponse(ResumeEducation education) {
        return ResumeEducationResponse.builder()
                .id(education.getId())
                .resumeId(education.getResume().getId())
                .schoolName(education.getSchoolName())
                .major(education.getMajor())
                .degree(education.getDegree())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .description(education.getDescription())
                .build();
    }

    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeExperienceResponse> handle(GetResumeExperiencesQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }

        List<ResumeExperience> experiences = resumeExperienceRepository.findAllByResumeId(query.getResumeId());
        return experiences.stream()
                .map(this::mapToExperienceResponse)
                .collect(Collectors.toList());
    }

    private ResumeExperienceResponse mapToExperienceResponse(ResumeExperience experience) {
        return ResumeExperienceResponse.builder()
                .id(experience.getId())
                .resumeId(experience.getResume().getId())
                .companyName(experience.getCompanyName())
                .position(experience.getPosition())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .currentJob(experience.getCurrentJob())
                .description(experience.getDescription())
                .build();
    }

    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeProjectResponse> handle(GetResumeProjectsQuery query) {
        Resume resume = resumeRepository.findById(query.getResumeId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại"));
        if (resume.getStatus() == ResumeStatus.DELETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Resume không tồn tại");
        }

        List<ResumeProject> projects = resumeProjectRepository.findAllByResumeId(query.getResumeId());
        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    private ResumeProjectResponse mapToProjectResponse(ResumeProject project) {
        return ResumeProjectResponse.builder()
                .id(project.getId())
                .resumeId(project.getResume().getId())
                .projectName(project.getProjectName())
                .role(project.getRole())
                .description(project.getDescription())
                .technologies(project.getTechnologies())
                .projectUrl(project.getProjectUrl())
                .build();
    }
}
