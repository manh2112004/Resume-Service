package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class AddResumeSkillCommand {
    @TargetAggregateIdentifier
    private final String resumeId;
    private final String skillId;
    private final String skillName;
    private final String level;
}
