package cn.eta.team.eta.resume;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.resume.ResumeDtos.QueryRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public Paged<Resume> list(String userId, QueryRequest q) {
        int page = PageUtils.page(q.page());
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size);

        Page<Resume> resumePage = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return Paged.of(resumePage);
    }

}