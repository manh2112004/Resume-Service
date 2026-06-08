package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class DeleteResumeExperienceCommand {
    @TargetAggregateIdentifier
    private final String resumeId;
    private final String experienceId;
}
