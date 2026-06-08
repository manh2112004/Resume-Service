package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, String> {
    List<ResumeEducation> findAllByResumeId(String resumeId);
}
