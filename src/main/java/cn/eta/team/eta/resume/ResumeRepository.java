package cn.eta.team.eta.resume;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    Page<Resume> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Resume> findByIdAndOwnerId(String id, String ownerId);
}