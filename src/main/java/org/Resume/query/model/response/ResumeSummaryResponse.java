package org.Resume.query.model.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSummaryResponse {
    private String resumeId;
    private String candidateId;
    private String fileName;
    private String fileUrl;
    private String status;
}
