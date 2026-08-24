package cn.eta.team.eta.resume;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.resume.ResumeDtos.CreateRequest;
import cn.eta.team.eta.resume.ResumeDtos.QueryRequest;
import cn.eta.team.eta.resume.ResumeDtos.UpdateRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        Page<Resume> resumePage = resumeRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable);

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

    public void delete(String userId, String id) {
        Resume resume = requireOwnedTask(id, userId);
        resumeRepository.delete(resume);
    }

    private Resume requireOwnedTask(String id, String ownerId) {
        return resumeRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public void export(String userId, String id, String format, HttpServletResponse response) {
        Resume resume = requireOwnedTask(id, userId);

        try {
            if ("json".equalsIgnoreCase(format)) {
                exportJson(response, resume);
            } else if ("pdf".equalsIgnoreCase(format)) {
                exportPdf(response, resume);
            } else if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
                exportMarkdown(response, resume);
            } else {
                throw new BizException(ErrorCode.RESUME_EXPORT_FAILED, "不支持的导出格式: " + format);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件导出失败", e);
        }
    }

    private void exportMarkdown(HttpServletResponse response, Resume resume) throws IOException {
        String fileName = "resume_" + resume.getId() + ".md";
        response.setContentType("text/markdown;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        StringBuilder md = new StringBuilder();
        md.append("# ").append(resume.getContent().getName()).append("\n\n");
        md.append("**岗位**: ").append(resume.getContent().getRole()).append("\n");
        md.append("**邮箱**: ").append(resume.getContent().getEmail()).append("\n");
        md.append("**电话**: ").append(resume.getContent().getPhone()).append("\n\n");

        if (resume.getContent() != null) {
            if (resume.getContent().getEducation() != null) {
                md.append("## 教育经历\n");
                for (var edu : resume.getContent().getEducation()) {
                    md.append("- **").append(edu.getTitle()).append("**");
                    if (edu.getSubtitle() != null)
                        md.append(" (").append(edu.getSubtitle()).append(")");
                    if (edu.getPeriod() != null)
                        md.append(" — ").append(edu.getPeriod());
                    md.append("\n");
                }
            }

            if (resume.getContent().getExperience() != null) {
                md.append("\n## 工作经历\n");
                for (var exp : resume.getContent().getExperience()) {
                    md.append("- **").append(exp.getTitle()).append("**");
                    if (exp.getSubtitle() != null)
                        md.append(" (").append(exp.getSubtitle()).append(")");
                    if (exp.getPeriod() != null)
                        md.append(" — ").append(exp.getPeriod());
                    md.append("\n");
                }
            }

            if (resume.getContent().getProjects() != null) {
                md.append("\n## 项目经历\n");
                for (var exp : resume.getContent().getProjects()) {
                    md.append("- **").append(exp.getTitle()).append("**");
                    if (exp.getSubtitle() != null)
                        md.append(" (").append(exp.getSubtitle()).append(")");
                    if (exp.getPeriod() != null)
                        md.append(" — ").append(exp.getPeriod());
                    md.append("\n");
                }
            }

            if (resume.getContent().getSkills() != null) {
                md.append("\n## 技能\n");
                md.append(String.join("  ", resume.getContent().getSkills()));
            }
        }

        response.getWriter().write(md.toString());
        response.getWriter().flush();
    }

    private void exportJson(HttpServletResponse response, Resume resume) throws IOException {
        String fileName = "resume_" + resume.getId() + ".json";

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        String json = JsonUtils.toJsonPretty(resume);
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    private void exportPdf(HttpServletResponse response, Resume resume) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exportPdf'");
    }
}