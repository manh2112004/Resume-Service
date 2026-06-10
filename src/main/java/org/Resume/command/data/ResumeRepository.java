package org.Resume.command.data;

import org.Resume.constant.ResumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {
    List<Resume> findAllByCandidateId(String candidateId);
    boolean existsByCandidateId(String candidateId);
    long countByCandidateId(String candidateId);
    Page<Resume> findAllByStatusNot(ResumeStatus status, Pageable pageable);
    java.util.Optional<Resume> findByCandidateIdAndIsDefaultTrueAndStatusNot(String candidateId, ResumeStatus status);

    long countByStatusNot(ResumeStatus status);
    long countByIsDefaultTrueAndStatusNot(ResumeStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r.fileType, COUNT(r) FROM Resume r WHERE r.status <> :status GROUP BY r.fileType")
    List<Object[]> countByFileTypeGrouped(@org.springframework.data.repository.query.Param("status") ResumeStatus status);
}

