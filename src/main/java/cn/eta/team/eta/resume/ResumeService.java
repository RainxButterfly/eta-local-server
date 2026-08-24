package cn.eta.team.eta.resume;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.resume.ResumeDtos.CreateRequest;
import cn.eta.team.eta.resume.ResumeDtos.QueryRequest;
import cn.eta.team.eta.resume.ResumeDtos.UpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public Paged<Resume> list(String ownerId, QueryRequest q) {
        int page = PageUtils.page(q.page() - 1);
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size);

        Page<Resume> resumePage = resumeRepository.findByUserIdOrderByCreatedAtDesc(ownerId, pageable);

        return Paged.of(resumePage);
    }

    @Transactional
    public Resume create(String ownerId, CreateRequest q) {
        Resume resume = new Resume();
        resume.setOwnerId(ownerId);
        resume.setName(q.name());
        resume.setTemplateId(q.templateId());

        resume.setContent(q.content());
        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public Resume detail(String id, String ownerId) {
        return requireOwnedTask(id, ownerId);
    }

    @Transactional
    public Resume update(String userId, String id, UpdateRequest q) {
        Resume resume = requireOwnedTask(id, userId);
        if (q.name() != null) {
            resume.setName(q.name());
        }
        if (q.content() != null) {
            resume.setContent(q.content());
        }
        resume.setUpdatedAt(Instant.now());
        return resumeRepository.save(resume);
    }

    private Resume requireOwnedTask(String id, String ownerId) {
        return resumeRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
    }

    public void delete(String userId, String id) {
        Resume resume = requireOwnedTask(id, userId);
        resumeRepository.delete(resume);
    }

    public void export(String userId, String id, String format) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'export'");
    }

}