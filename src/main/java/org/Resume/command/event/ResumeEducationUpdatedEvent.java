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
public class ResumeEducationUpdatedEvent {
    private String resumeId;
    private String educationId;
    private String schoolName;
    private String major;
    private String degree;
    private String startDate;
    private String endDate;
    private String description;
}
