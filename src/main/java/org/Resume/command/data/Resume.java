package org.Resume.command.data;

import jakarta.persistence.*;
import lombok.*;
import org.Resume.constant.ResumeStatus;

import java.time.LocalDateTime;
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    private String id;

    @Column(nullable = false)
    private String candidateId;
    // Lấy từ User/Profile Service

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    private String fileType;
    // pdf, docx

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private ResumeStatus status;

    private Boolean isDefault;

    private LocalDateTime uploadedAt;

    private LocalDateTime updatedAt;
}