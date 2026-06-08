package org.Resume.query.controller;

import org.Resume.query.queries.GetResumeByIdQuery;
import org.Resume.query.model.response.ResumeResponse;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/internal/resumes")
public class ResumeInternalQueryController {

    @Autowired
    private QueryGateway queryGateway;

    @GetMapping("/{resumeId}/exists")
    public CompletableFuture<Boolean> checkResumeExists(@PathVariable String resumeId) {
        return queryGateway.query(
                new GetResumeByIdQuery(resumeId),
                ResponseTypes.instanceOf(ResumeResponse.class)
        ).thenApply(res -> true).exceptionally(err -> false);
    }
}
