package org.Resume.command.data;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "resume_parsed_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeParsedData {

    @Id
    private String id;

    @OneToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String fullName;

    private String email;

    private String phone;

    private String address;

    private String summary;

    private Integer totalExperienceYears;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    private LocalDateTime parsedAt;
}