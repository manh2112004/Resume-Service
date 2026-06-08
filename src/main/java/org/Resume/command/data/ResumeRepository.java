package org.Resume.command.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    List<Resume> findAllByCandidateId(String candidateId);
    boolean existsByCandidateId(String candidateId);
    long countByCandidateId(String candidateId);
}
