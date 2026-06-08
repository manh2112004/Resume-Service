package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProject, String> {
    List<ResumeProject> findAllByResumeId(String resumeId);
}
