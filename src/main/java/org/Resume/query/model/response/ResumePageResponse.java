package org.Resume.query.model.response;

import lombok.NoArgsConstructor;
import java.util.List;

@NoArgsConstructor
public class ResumePageResponse extends PageResponse<ResumeResponse> {
    public ResumePageResponse(List<ResumeResponse> content, int page, int size, long totalElements, int totalPages) {
        super(content, page, size, totalElements, totalPages);
    }
}
