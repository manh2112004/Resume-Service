package org.Resume.command.event;

import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
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
}
