package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, String> {
    List<ResumeExperience> findAllByResumeId(String resumeId);
}
