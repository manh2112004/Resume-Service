package org.Resume.query.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeStatisticsResponse {
    private long totalResumes;
    private long defaultResumes;
    private Map<String, Long> resumesByType;
}
