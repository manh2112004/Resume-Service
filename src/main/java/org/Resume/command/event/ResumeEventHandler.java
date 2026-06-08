package org.Resume.command.event;

import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.Resume.command.data.ResumeSkill;
import org.Resume.command.data.ResumeSkillRepository;
import org.Resume.command.data.ResumeEducation;
import org.Resume.command.data.ResumeEducationRepository;
import org.Resume.constant.ResumeStatus;
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
}
