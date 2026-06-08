package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class AddResumeProjectCommand {
    @TargetAggregateIdentifier
    private final String resumeId;
    private final String projectId;
    private final String projectName;
    private final String role;
    private final String description;
    private final String technologies;
    private final String projectUrl;
}
