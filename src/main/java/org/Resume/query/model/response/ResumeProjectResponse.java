package org.Resume.query.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeProjectResponse {
    private String id;
    private String resumeId;
    private String projectName;
    private String role;
    private String description;
    private String technologies;
    private String projectUrl;
}
