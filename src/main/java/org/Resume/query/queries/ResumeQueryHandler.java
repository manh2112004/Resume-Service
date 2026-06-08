package org.Resume.query.queries;

import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.Resume.query.model.response.ResumeResponse;
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

    @QueryHandler
    @Transactional(readOnly = true)
    public List<ResumeResponse> handle(GetMyResumesQuery query) {
        List<Resume> resumes = resumeRepository.findAllByCandidateId(query.getCandidateId());
        return resumes.stream()
                .map(this::mapToResponse)
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
}
