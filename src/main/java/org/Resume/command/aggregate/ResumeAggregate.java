package org.Resume.command.aggregate;

import org.Resume.command.command.CreateResumeCommand;
import org.Resume.command.command.SetDefaultResumeCommand;
import org.Resume.command.command.DeleteResumeCommand;
import org.Resume.command.event.ResumeCreatedEvent;
import org.Resume.command.event.ResumeDefaultSetEvent;
import org.Resume.command.event.ResumeDeletedEvent;
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

    @CommandHandler
    public void handle(SetDefaultResumeCommand command) {
        AggregateLifecycle.apply(ResumeDefaultSetEvent.builder()
                .id(command.getId())
                .candidateId(command.getCandidateId())
                .build());
    }

    @CommandHandler
    public void handle(DeleteResumeCommand command) {
        AggregateLifecycle.apply(ResumeDeletedEvent.builder()
                .id(command.getId())
                .candidateId(command.getCandidateId())
                .build());
    }

    @EventSourcingHandler
    public void on(ResumeCreatedEvent event) {
        this.id = event.getId();
    }

    @EventSourcingHandler
    public void on(ResumeDefaultSetEvent event) {
        this.id = event.getId();
    }

    @EventSourcingHandler
    public void on(ResumeDeletedEvent event) {
        AggregateLifecycle.markDeleted();
    }
}
