package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeParsedDataRepository extends JpaRepository<ResumeParsedData, String> {
    Optional<ResumeParsedData> findByResumeId(String resumeId);
}
