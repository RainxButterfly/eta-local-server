package cn.eta.team.eta.domain.resume;

import java.io.IOException;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.FileStorageUtils;
import cn.eta.team.eta.common.util.JsonUtils;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.domain.resume.ResumeDtos.CreateRequest;
import cn.eta.team.eta.domain.resume.ResumeDtos.QueryRequest;
import cn.eta.team.eta.domain.resume.ResumeDtos.UpdateRequest;
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
    public String export(String userId, String id, String format) {
        Resume resume = requireOwnedTask(id, userId);
        byte[] fileContent;
        String fileName = "resume_" + resume.getId() + "." + format;
        try {
            if ("json".equalsIgnoreCase(format)) {
                fileContent = exportJson(resume);
            } else if ("pdf".equalsIgnoreCase(format)) {
                fileContent = exportPdf(resume);
            } else if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
                fileContent = exportMarkdown(resume);
            } else {
                throw new BizException(ErrorCode.RESUME_EXPORT_FAILED, "不支持的导出格式: " + format);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件导出失败", e);
        }
        FileStorageUtils.saveToLocal(fileContent, fileName);
        return fileName;
    }

    private byte[] exportMarkdown(Resume resume) throws IOException {
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
        return md.toString().getBytes();
    }

    private byte[] exportJson(Resume resume) throws IOException {
        String json = JsonUtils.toJsonPretty(resume);
        return json.getBytes();
    }

    private byte[] exportPdf(Resume resume) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'exportPdf'");
    }
}