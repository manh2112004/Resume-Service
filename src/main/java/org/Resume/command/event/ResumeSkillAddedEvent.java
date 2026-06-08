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
public class ResumeSkillAddedEvent {
    private String resumeId;
    private String id; // Fallback for resumeId
    private String skillId;
    private String skillName;
    private String level;
    private String candidateId;

    public String getResumeId() {
        return resumeId != null ? resumeId : id;
    }
}
