package org.Resume.command.data;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "resume_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeSkill {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String skillName;

    private String level;
    // BEGINNER, INTERMEDIATE, ADVANCED
}
