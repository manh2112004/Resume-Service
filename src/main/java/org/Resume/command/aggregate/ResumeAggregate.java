package org.Resume.command.aggregate;

import org.Resume.command.command.*;
import org.Resume.command.event.ResumeCreatedEvent;
import org.Resume.command.event.ResumeDefaultSetEvent;
import org.Resume.command.event.ResumeDeletedEvent;
import org.Resume.command.event.ResumeSkillAddedEvent;
import org.Resume.command.event.ResumeSkillUpdatedEvent;
import org.Resume.command.event.ResumeSkillDeletedEvent;
import org.Resume.command.event.ResumeEducationAddedEvent;
import org.Resume.command.event.ResumeEducationUpdatedEvent;
import org.Resume.command.event.ResumeEducationDeletedEvent;
import org.Resume.command.event.ResumeExperienceAddedEvent;
import org.Resume.command.event.ResumeExperienceUpdatedEvent;
import org.Resume.command.event.ResumeExperienceDeletedEvent;
import org.Resume.command.event.ResumeProjectAddedEvent;
import org.Resume.command.event.ResumeProjectUpdatedEvent;
import org.Resume.command.event.ResumeProjectDeletedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import org.Resume.command.command.ParseResumeCommand;
import org.Resume.command.event.ResumeParsedEvent;

@Aggregate
public class ResumeAggregate {

    @AggregateIdentifier
    private String id;

    public ResumeAggregate() {
        // Required by Axon
    }

    @CommandHandler
    public void handle(ParseResumeCommand command) {
        AggregateLifecycle.apply(ResumeParsedEvent.builder()
                .id(command.getId())
                .fullName(command.getFullName())
                .email(command.getEmail())
                .phone(command.getPhone())
                .address(command.getAddress())
                .summary(command.getSummary())
                .totalExperienceYears(command.getTotalExperienceYears())
                .rawText(command.getRawText())
                .build());
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

    @CommandHandler
    public void handle(AddResumeExperienceCommand command) {
        AggregateLifecycle.apply(ResumeExperienceAddedEvent.builder()
                .resumeId(command.getResumeId())
                .experienceId(command.getExperienceId())
                .companyName(command.getCompanyName())
                .position(command.getPosition())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .currentJob(command.getCurrentJob())
                .description(command.getDescription())
                .build());
    }

    @CommandHandler
    public void handle(UpdateResumeExperienceCommand command) {
        AggregateLifecycle.apply(ResumeExperienceUpdatedEvent.builder()
                .resumeId(command.getResumeId())
                .experienceId(command.getExperienceId())
                .companyName(command.getCompanyName())
                .position(command.getPosition())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .currentJob(command.getCurrentJob())
                .description(command.getDescription())
                .build());
    }

    @CommandHandler
    public void handle(DeleteResumeExperienceCommand command) {
        AggregateLifecycle.apply(ResumeExperienceDeletedEvent.builder()
                .resumeId(command.getResumeId())
                .experienceId(command.getExperienceId())
                .build());
    }

    @CommandHandler
    public void handle(AddResumeProjectCommand command) {
        AggregateLifecycle.apply(ResumeProjectAddedEvent.builder()
                .resumeId(command.getResumeId())
                .projectId(command.getProjectId())
                .projectName(command.getProjectName())
                .role(command.getRole())
                .description(command.getDescription())
                .technologies(command.getTechnologies())
                .projectUrl(command.getProjectUrl())
                .build());
    }

    @CommandHandler
    public void handle(UpdateResumeProjectCommand command) {
        AggregateLifecycle.apply(ResumeProjectUpdatedEvent.builder()
                .resumeId(command.getResumeId())
                .projectId(command.getProjectId())
                .projectName(command.getProjectName())
                .role(command.getRole())
                .description(command.getDescription())
                .technologies(command.getTechnologies())
                .projectUrl(command.getProjectUrl())
                .build());
    }

    @CommandHandler
    public void handle(DeleteResumeProjectCommand command) {
        AggregateLifecycle.apply(ResumeProjectDeletedEvent.builder()
                .resumeId(command.getResumeId())
                .projectId(command.getProjectId())
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

    @EventSourcingHandler
    public void on(ResumeExperienceAddedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeExperienceUpdatedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeExperienceDeletedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeProjectAddedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeProjectUpdatedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeProjectDeletedEvent event) {
        // No-op
    }

    @EventSourcingHandler
    public void on(ResumeParsedEvent event) {
        // No-op
    }
}
