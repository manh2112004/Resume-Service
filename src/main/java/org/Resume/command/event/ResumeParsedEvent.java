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
public class ResumeParsedEvent {
    private String id; // resumeId
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String summary;
    private Integer totalExperienceYears;
    private String rawText;
}
