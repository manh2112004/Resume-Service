package org.Resume.command.event;

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
import org.Resume.constant.ResumeStatus;
import org.Resume.command.data.ResumeParsedData;
import org.Resume.command.data.ResumeParsedDataRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ResumeEventHandler {

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

    @EventHandler
    public void on(ResumeCreatedEvent event) {
        if (Boolean.TRUE.equals(event.getIsDefault())) {
            List<Resume> existingResumes = resumeRepository.findAllByCandidateId(event.getCandidateId());
            for (Resume r : existingResumes) {
                if (Boolean.TRUE.equals(r.getIsDefault())) {
                    r.setIsDefault(false);
                    r.setUpdatedAt(LocalDateTime.now());
                    resumeRepository.save(r);
                }
            }
        }

        Resume resume = Resume.builder()
                .id(event.getId())
                .candidateId(event.getCandidateId())
                .fileName(event.getFileName())
                .fileUrl(event.getFileUrl())
                .fileType(event.getFileType())
                .fileSize(event.getFileSize())
                .status(ResumeStatus.UPLOADED)
                .isDefault(event.getIsDefault())
                .uploadedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        resumeRepository.save(resume);
    }

    @EventHandler
    public void on(ResumeDefaultSetEvent event) {
        List<Resume> existingResumes = resumeRepository.findAllByCandidateId(event.getCandidateId());
        for (Resume r : existingResumes) {
            if (r.getId().equals(event.getId())) {
                r.setIsDefault(true);
            } else {
                r.setIsDefault(false);
            }
            r.setUpdatedAt(LocalDateTime.now());
            resumeRepository.save(r);
        }
    }

    @EventHandler
    public void on(ResumeDeletedEvent event) {
        resumeRepository.findById(event.getId()).ifPresent(resume -> {
            boolean wasDefault = Boolean.TRUE.equals(resume.getIsDefault());
            resume.setStatus(ResumeStatus.DELETED);
            resume.setIsDefault(false);
            resume.setUpdatedAt(LocalDateTime.now());
            resumeRepository.save(resume);

            if (wasDefault) {
                List<Resume> remaining = resumeRepository.findAllByCandidateId(event.getCandidateId());
                Resume newDefault = null;
                for (Resume r : remaining) {
                    if (r.getStatus() != ResumeStatus.DELETED && !r.getId().equals(event.getId())) {
                        newDefault = r;
                        break;
                    }
                }
                if (newDefault != null) {
                    newDefault.setIsDefault(true);
                    newDefault.setUpdatedAt(LocalDateTime.now());
                    resumeRepository.save(newDefault);
                }
            }
        });
    }

    @EventHandler
    public void on(ResumeSkillAddedEvent event) {
        resumeRepository.findById(event.getResumeId()).ifPresent(resume -> {
            ResumeSkill skill = ResumeSkill.builder()
                    .id(event.getSkillId())
                    .resume(resume)
                    .skillName(event.getSkillName())
                    .level(event.getLevel())
                    .build();
            resumeSkillRepository.save(skill);
        });
    }

    @EventHandler
    public void on(ResumeSkillUpdatedEvent event) {
        resumeSkillRepository.findById(event.getSkillId()).ifPresent(skill -> {
            skill.setSkillName(event.getSkillName());
            skill.setLevel(event.getLevel());
            resumeSkillRepository.save(skill);
        });
    }

    @EventHandler
    public void on(ResumeSkillDeletedEvent event) {
        resumeSkillRepository.deleteById(event.getSkillId());
    }

    @EventHandler
    public void on(ResumeEducationAddedEvent event) {
        resumeRepository.findById(event.getResumeId()).ifPresent(resume -> {
            ResumeEducation education = ResumeEducation.builder()
                    .id(event.getEducationId())
                    .resume(resume)
                    .schoolName(event.getSchoolName())
                    .major(event.getMajor())
                    .degree(event.getDegree())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .description(event.getDescription())
                    .build();
            resumeEducationRepository.save(education);
        });
    }

    @EventHandler
    public void on(ResumeEducationUpdatedEvent event) {
        resumeEducationRepository.findById(event.getEducationId()).ifPresent(education -> {
            education.setSchoolName(event.getSchoolName());
            education.setMajor(event.getMajor());
            education.setDegree(event.getDegree());
            education.setStartDate(event.getStartDate());
            education.setEndDate(event.getEndDate());
            education.setDescription(event.getDescription());
            resumeEducationRepository.save(education);
        });
    }

    @EventHandler
    public void on(ResumeEducationDeletedEvent event) {
        resumeEducationRepository.deleteById(event.getEducationId());
    }

    @EventHandler
    public void on(ResumeExperienceAddedEvent event) {
        resumeRepository.findById(event.getResumeId()).ifPresent(resume -> {
            ResumeExperience experience = ResumeExperience.builder()
                    .id(event.getExperienceId())
                    .resume(resume)
                    .companyName(event.getCompanyName())
                    .position(event.getPosition())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .currentJob(event.getCurrentJob())
                    .description(event.getDescription())
                    .build();
            resumeExperienceRepository.save(experience);
        });
    }

    @EventHandler
    public void on(ResumeExperienceUpdatedEvent event) {
        resumeExperienceRepository.findById(event.getExperienceId()).ifPresent(experience -> {
            experience.setCompanyName(event.getCompanyName());
            experience.setPosition(event.getPosition());
            experience.setStartDate(event.getStartDate());
            experience.setEndDate(event.getEndDate());
            experience.setCurrentJob(event.getCurrentJob());
            experience.setDescription(event.getDescription());
            resumeExperienceRepository.save(experience);
        });
    }

    @EventHandler
    public void on(ResumeExperienceDeletedEvent event) {
        resumeExperienceRepository.deleteById(event.getExperienceId());
    }

    @EventHandler
    public void on(ResumeProjectAddedEvent event) {
        resumeRepository.findById(event.getResumeId()).ifPresent(resume -> {
            ResumeProject project = ResumeProject.builder()
                    .id(event.getProjectId())
                    .resume(resume)
                    .projectName(event.getProjectName())
                    .role(event.getRole())
                    .description(event.getDescription())
                    .technologies(event.getTechnologies())
                    .projectUrl(event.getProjectUrl())
                    .build();
            resumeProjectRepository.save(project);
        });
    }

    @EventHandler
    public void on(ResumeProjectUpdatedEvent event) {
        resumeProjectRepository.findById(event.getProjectId()).ifPresent(project -> {
            project.setProjectName(event.getProjectName());
            project.setRole(event.getRole());
            project.setDescription(event.getDescription());
            project.setTechnologies(event.getTechnologies());
            project.setProjectUrl(event.getProjectUrl());
            resumeProjectRepository.save(project);
        });
    }

    @EventHandler
    public void on(ResumeProjectDeletedEvent event) {
        resumeProjectRepository.deleteById(event.getProjectId());
    }

    @EventHandler
    public void on(ResumeParsedEvent event) {
        resumeRepository.findById(event.getId()).ifPresent(resume -> {
            ResumeParsedData parsedData = resumeParsedDataRepository.findByResumeId(event.getId())
                    .orElse(new ResumeParsedData());
            
            if (parsedData.getId() == null) {
                parsedData.setId(java.util.UUID.randomUUID().toString());
                parsedData.setResume(resume);
            }
            
            parsedData.setFullName(event.getFullName());
            parsedData.setEmail(event.getEmail());
            parsedData.setPhone(event.getPhone());
            parsedData.setAddress(event.getAddress());
            parsedData.setSummary(event.getSummary());
            parsedData.setTotalExperienceYears(event.getTotalExperienceYears());
            parsedData.setRawText(event.getRawText());
            parsedData.setParsedAt(LocalDateTime.now());
            
            resumeParsedDataRepository.save(parsedData);
        });
    }
}
