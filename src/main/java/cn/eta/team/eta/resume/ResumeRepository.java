package cn.eta.team.eta.resume;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, String> {

    //一并加入查询，防止LazyInitializationException
    @EntityGraph(attributePaths = {
        "content.education", 
        "content.experience", 
        "content.projects", 
        "content.skills"
    })
    Page<Resume> findByOwnerIdOrderByCreatedAtDesc(String ownerId, Pageable pageable);

    //一并加入查询，防止LazyInitializationException
    @EntityGraph(attributePaths = {
        "content.education", 
        "content.experience", 
        "content.projects", 
        "content.skills"
    })
    Optional<Resume> findByIdAndOwnerId(String id, String ownerId);
}