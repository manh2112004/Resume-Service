package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class AddResumeEducationCommand {
    @TargetAggregateIdentifier
    private final String resumeId;
    private final String educationId;
    private final String schoolName;
    private final String major;
    private final String degree;
    private final String startDate;
    private final String endDate;
    private final String description;
}
