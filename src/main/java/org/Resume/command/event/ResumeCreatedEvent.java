package org.Resume.command.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeCreatedEvent {
    private String id;
    private String candidateId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Boolean isDefault;
}
