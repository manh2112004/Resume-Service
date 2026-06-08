package org.Resume.command.aggregate;

import org.Resume.command.command.CreateResumeCommand;
import org.Resume.command.event.ResumeCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class ResumeAggregate {

    @AggregateIdentifier
    private String id;

    public ResumeAggregate() {
        // Required by Axon
    }

    @CommandHandler
    public ResumeAggregate(CreateResumeCommand command) {
        AggregateLifecycle.apply(ResumeCreatedEvent.builder()
                .id(command.getId())
                .candidateId(command.getCandidateId())
                .fileName(command.getFileName())
                .fileUrl(command.getFileUrl())
                .fileType(command.getFileType())
                .fileSize(command.getFileSize())
                .isDefault(command.getIsDefault())
                .build());
    }

    @EventSourcingHandler
    public void on(ResumeCreatedEvent event) {
        this.id = event.getId();
    }
}
