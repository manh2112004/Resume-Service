package org.Resume.command.command;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@Builder
public class CreateResumeCommand {
    @TargetAggregateIdentifier
    private final String id;
    private final String candidateId;
    private final String fileName;
    private final String fileUrl;
    private final String fileType;
    private final Long fileSize;
    private final Boolean isDefault;
}
