package org.Resume.query.model.response;

import lombok.*;
import org.Resume.constant.ResumeStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeResponse {
    private String id;
    private String candidateId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private ResumeStatus status;
    private Boolean isDefault;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}
