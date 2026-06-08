package org.Resume.query.model.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeIndexDataResponse {
    private String resumeId;
    private String candidateId;
    private List<String> skills;
    private List<ResumeEducationResponse> education;
    private List<ResumeExperienceResponse> experience;
    private List<ResumeProjectResponse> projects;
    private String rawText;
}
