package org.Resume.command.controller;

import org.Resume.command.command.DeleteResumeCommand;
import org.Resume.command.data.Resume;
import org.Resume.command.data.ResumeRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/resumes")
public class AdminResumeCommandController {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private ResumeRepository resumeRepository;

    @DeleteMapping("/{resumeId}")
    public CompletableFuture<Void> deleteResume(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String resumeId
    ) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được user từ token");
        }

        if (!hasAdminRole(jwt)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV"));

        if (resume.getStatus() == org.Resume.constant.ResumeStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV");
        }

        DeleteResumeCommand command = DeleteResumeCommand.builder()
                .id(resumeId)
                .candidateId(resume.getCandidateId())
                .build();

        return commandGateway.send(command).thenApply(result -> null);
    }

    private boolean hasAdminRole(Jwt jwt) {
        if (hasRoleInRealmAccess(jwt, "SYSTEM_ADMIN", "ROLE_SYSTEM_ADMIN", "ADMIN", "ROLE_ADMIN")) {
            return true;
        }
        if (hasRoleInResourceAccess(jwt, "SYSTEM_ADMIN", "ROLE_SYSTEM_ADMIN", "ADMIN", "ROLE_ADMIN")) {
            return true;
        }
        if (containsRole(jwt.getClaim("authorities"), "SYSTEM_ADMIN", "ROLE_SYSTEM_ADMIN", "ADMIN", "ROLE_ADMIN")) {
            return true;
        }

        String scope = jwt.getClaimAsString("scope");
        if (scope != null && Arrays.stream(scope.split("\\s+"))
                .map(String::trim)
                .anyMatch(s -> "system_admin".equalsIgnoreCase(s) 
                        || "role_system_admin".equalsIgnoreCase(s) 
                        || "admin".equalsIgnoreCase(s) 
                        || "role_admin".equalsIgnoreCase(s))) {
            return true;
        }

        Object scpClaim = jwt.getClaim("scp");
        return containsRole(scpClaim, "system_admin", "role_system_admin", "admin", "role_admin");
    }

    private boolean hasRoleInRealmAccess(Jwt jwt, String... expectedRoles) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> realmMap)) {
            return false;
        }
        return containsRole(realmMap.get("roles"), expectedRoles);
    }

    private boolean hasRoleInResourceAccess(Jwt jwt, String... expectedRoles) {
        Object resourceAccess = jwt.getClaim("resource_access");
        if (!(resourceAccess instanceof Map<?, ?> resourceMap)) {
            return false;
        }

        String preferredClient = jwt.getClaimAsString("azp");
        if (preferredClient != null) {
            Object clientAccess = resourceMap.get(preferredClient);
            if (containsRole(clientAccess, expectedRoles)) {
                return true;
            }
        }

        return resourceMap.values().stream().anyMatch(value -> containsRole(value, expectedRoles));
    }

    private boolean containsRole(Object claimValue, String... expectedRoles) {
        Set<String> expected = Arrays.stream(expectedRoles)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (claimValue instanceof Collection<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(String::toLowerCase)
                    .anyMatch(expected::contains);
        }

        if (claimValue instanceof Map<?, ?> mapClaim) {
            Object directRoles = mapClaim.get("roles");
            if (containsRole(directRoles, expectedRoles)) {
                return true;
            }
            return mapClaim.values().stream().anyMatch(v -> containsRole(v, expectedRoles));
        }

        return false;
    }
}
