package org.Resume.command.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeProjectUpdatedEvent {
    private String resumeId;
    private String projectId;
    private String projectName;
    private String role;
    private String description;
    private String technologies;
    private String projectUrl;
}
