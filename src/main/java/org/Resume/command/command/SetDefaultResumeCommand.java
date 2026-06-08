package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class SetDefaultResumeCommand {
    @TargetAggregateIdentifier
    private final String id;
    private final String candidateId;
}
