package org.Resume.command.aggregate;

import org.Resume.command.command.CreateResumeCommand;
import org.Resume.command.command.SetDefaultResumeCommand;
import org.Resume.command.command.DeleteResumeCommand;
import org.Resume.command.command.AddResumeSkillCommand;
import org.Resume.command.command.UpdateResumeSkillCommand;
import org.Resume.command.command.DeleteResumeSkillCommand;
import org.Resume.command.command.AddResumeEducationCommand;
import org.Resume.command.command.UpdateResumeEducationCommand;
import org.Resume.command.command.DeleteResumeEducationCommand;
import org.Resume.command.event.ResumeCreatedEvent;
import org.Resume.command.event.ResumeDefaultSetEvent;
import org.Resume.command.event.ResumeDeletedEvent;
import org.Resume.command.event.ResumeSkillAddedEvent;
import org.Resume.command.event.ResumeSkillUpdatedEvent;
import org.Resume.command.event.ResumeSkillDeletedEvent;
import org.Resume.command.event.ResumeEducationAddedEvent;
import org.Resume.command.event.ResumeEducationUpdatedEvent;
import org.Resume.command.event.ResumeEducationDeletedEvent;
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

    @CommandHandler
    public void handle(AddResumeSkillCommand command) {
        AggregateLifecycle.apply(ResumeSkillAddedEvent.builder()
                .resumeId(command.getResumeId())
                .skillId(command.getSkillId())
                .skillName(command.getSkillName())
                .level(command.getLevel())
                .build());
    }

    @CommandHandler
    public void handle(UpdateResumeSkillCommand command) {
        AggregateLifecycle.apply(ResumeSkillUpdatedEvent.builder()
                .resumeId(command.getResumeId())
                .skillId(command.getSkillId())
                .skillName(command.getSkillName())
                .level(command.getLevel())
                .build());
    }

    @CommandHandler
    public void handle(DeleteResumeSkillCommand command) {
        AggregateLifecycle.apply(ResumeSkillDeletedEvent.builder()
                .resumeId(command.getResumeId())
                .skillId(command.getSkillId())
                .build());
    }

    @CommandHandler
    public void handle(AddResumeEducationCommand command) {
        AggregateLifecycle.apply(ResumeEducationAddedEvent.builder()
                .resumeId(command.getResumeId())
                .educationId(command.getEducationId())
                .schoolName(command.getSchoolName())
                .major(command.getMajor())
                .degree(command.getDegree())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .description(command.getDescription())
                .build());
    }

    @CommandHandler
    public void handle(UpdateResumeEducationCommand command) {
        AggregateLifecycle.apply(ResumeEducationUpdatedEvent.builder()
                .resumeId(command.getResumeId())
                .educationId(command.getEducationId())
                .schoolName(command.getSchoolName())
                .major(command.getMajor())
                .degree(command.getDegree())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .description(command.getDescription())
                .build());
    }

    @CommandHandler
    public void handle(DeleteResumeEducationCommand command) {
        AggregateLifecycle.apply(ResumeEducationDeletedEvent.builder()
                .resumeId(command.getResumeId())
                .educationId(command.getEducationId())
                .build());
    }

    @EventSourcingHandler
    public void on(ResumeCreatedEvent event) {
        this.id = event.getId();
    }

    @EventSourcingHandler
    public void on(ResumeDeletedEvent event) {
        AggregateLifecycle.markDeleted();
    }

    @EventSourcingHandler
    public void on(ResumeEducationAddedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeEducationUpdatedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeEducationDeletedEvent event) {
        // No-op
    }
}
