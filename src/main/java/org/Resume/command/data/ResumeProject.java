package org.Resume.command.data;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "resume_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeProject {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String projectName;

    private String role;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String technologies;

    private String projectUrl;
}