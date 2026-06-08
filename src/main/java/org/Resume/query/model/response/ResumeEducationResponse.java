package org.Resume.query.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeEducationResponse {
    private String id;
    private String resumeId;
    private String schoolName;
    private String major;
    private String degree;
    private String startDate;
    private String endDate;
    private String description;
}
