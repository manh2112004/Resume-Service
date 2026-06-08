package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import java.time.LocalDate;

@Data
@Builder
public class AddResumeExperienceCommand {
    @TargetAggregateIdentifier
    private final String resumeId;
    private final String experienceId;
    private final String companyName;
    private final String position;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Boolean currentJob;
    private final String description;
}
