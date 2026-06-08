package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, String> {
    List<ResumeSkill> findAllByResumeId(String resumeId);
}
