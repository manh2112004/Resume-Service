package org.Resume.command.data;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Table(name = "resume_experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeExperience {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String companyName;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean currentJob;

    @Column(columnDefinition = "TEXT")
    private String description;
}
