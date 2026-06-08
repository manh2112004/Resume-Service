package org.Resume.query.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSkillResponse {
    private String id;
    private String resumeId;
    private String skillName;
    private String level;
}
