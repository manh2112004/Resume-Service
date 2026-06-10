package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class ParseResumeCommand {
    @TargetAggregateIdentifier
    private final String id; // resumeId
    private final String fullName;
    private final String email;
    private final String phone;
    private final String address;
    private final String summary;
    private final Integer totalExperienceYears;
    private final String rawText;
}
