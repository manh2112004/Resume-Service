package org.Resume.query.controller;

import org.Resume.query.model.response.ResumeResponse;
import org.Resume.query.queries.GetMyResumesQuery;
import org.Resume.query.queries.GetResumeByIdQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeQueryController {

    @Autowired
    private QueryGateway queryGateway;

    @GetMapping("/me")
    public CompletableFuture<List<ResumeResponse>> getMyResumes(@AuthenticationPrincipal Jwt jwt) {
        return queryGateway.query(
                new GetMyResumesQuery(jwt.getSubject()),
                ResponseTypes.multipleInstancesOf(ResumeResponse.class)
        );
    }

    @GetMapping("/{resumeId}")
    public CompletableFuture<ResumeResponse> getResumeById(@PathVariable String resumeId) {
        return queryGateway.query(
                new GetResumeByIdQuery(resumeId),
                ResponseTypes.instanceOf(ResumeResponse.class)
        );
    }

    @GetMapping("/{resumeId}/skills")
    public CompletableFuture<List<org.Resume.query.model.response.ResumeSkillResponse>> getResumeSkills(
            @PathVariable String resumeId
    ) {
        return queryGateway.query(
                new org.Resume.query.queries.GetResumeSkillsQuery(resumeId),
                ResponseTypes.multipleInstancesOf(org.Resume.query.model.response.ResumeSkillResponse.class)
        );
    }

    @GetMapping("/{resumeId}/educations")
    public CompletableFuture<List<org.Resume.query.model.response.ResumeEducationResponse>> getResumeEducations(
            @PathVariable String resumeId
    ) {
        return queryGateway.query(
                new org.Resume.query.queries.GetResumeEducationsQuery(resumeId),
                ResponseTypes.multipleInstancesOf(org.Resume.query.model.response.ResumeEducationResponse.class)
        );
    }
}
