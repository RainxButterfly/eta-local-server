// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
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
        int pageNum = q.page() != null ? q.page() - 1 : 0;
        int page = PageUtils.page(pageNum);
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size);

        Page<Resume> resumePage = resumeRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId, pageable);
        return Paged.of(resumePage);
    }

    @Transactional
    public Resume create(String ownerId, CreateRequest q) {
        Resume resume = new Resume();
        resume.setName(q.name());
        resume.setTemplateId(q.templateId());
        resume.setContent(q.content());
        resume.setOwnerId(ownerId);
        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public Resume detail(String id) {
        return requireResume(id);
    }

    @Transactional
    public Resume update(String id, UpdateRequest q) {
        Resume resume = requireResume(id);
        if (q.name() != null) {
            resume.setName(q.name());
        }
        if (q.content() != null) {
            resume.setContent(q.content());
        }
        resume.setUpdatedAt(Instant.now());
        return resumeRepository.save(resume);
    }

    @Transactional
    public void delete(String id) {
        Resume resume = requireResume(id);
        resume.setDeleted(true);
        resume.setUpdatedAt(Instant.now());
        resumeRepository.save(resume);
    }

    private Resume requireResume(String id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public String export(String id, String format) {
        Resume resume = requireResume(id);
        byte[] fileContent;
        String fileName = "resume_" + resume.getId() + "." + format;
        try {
            if ("json".equalsIgnoreCase(format)) {
                fileContent = exportJson(resume);
            } else if ("pdf".equalsIgnoreCase(format)) {
                throw new BizException(ErrorCode.RESUME_EXPORT_FAILED, "PDF 导出暂未支持");
            } else if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
                fileContent = exportMarkdown(resume);
            } else {
                throw new BizException(ErrorCode.RESUME_EXPORT_FAILED, "不支持的导出格式: " + format);
            }
        } catch (IOException e) {
            throw new BizException(ErrorCode.RESUME_EXPORT_FAILED, "文件导出失败");
        }
        FileStorageUtils.saveToLocal(fileContent, fileName);
        return fileName;
    }

    private byte[] exportMarkdown(Resume resume) {
        ResumeContent content = resume.getContent();
        StringBuilder md = new StringBuilder();
        if (content != null) {
            md.append("# ").append(content.getName()).append("\n\n");
            md.append("**岗位**: ").append(content.getRole()).append("\n");
            md.append("**邮箱**: ").append(content.getEmail()).append("\n");
            md.append("**电话**: ").append(content.getPhone()).append("\n\n");

            if (content.getEducation() != null) {
                md.append("## 教育经历\n");
                for (var edu : content.getEducation()) {
                    md.append("- **").append(edu.getTitle()).append("**");
                    if (edu.getSubtitle() != null)
                        md.append(" (").append(edu.getSubtitle()).append(")");
                    if (edu.getPeriod() != null)
                        md.append(" — ").append(edu.getPeriod());
                    md.append("\n");
                }
            }

            if (content.getExperience() != null) {
                md.append("\n## 工作经历\n");
                for (var exp : content.getExperience()) {
                    md.append("- **").append(exp.getTitle()).append("**");
                    if (exp.getSubtitle() != null)
                        md.append(" (").append(exp.getSubtitle()).append(")");
                    if (exp.getPeriod() != null)
                        md.append(" — ").append(exp.getPeriod());
                    md.append("\n");
                }
            }

            if (content.getProjects() != null) {
                md.append("\n## 项目经历\n");
                for (var exp : content.getProjects()) {
                    md.append("- **").append(exp.getTitle()).append("**");
                    if (exp.getSubtitle() != null)
                        md.append(" (").append(exp.getSubtitle()).append(")");
                    if (exp.getPeriod() != null)
                        md.append(" — ").append(exp.getPeriod());
                    md.append("\n");
                }
            }

            if (content.getSkills() != null) {
                md.append("\n## 技能\n");
                md.append(String.join("  ", content.getSkills()));
            }
        }
        return md.toString().getBytes();
    }

    private byte[] exportJson(Resume resume) throws IOException {
        String json = JsonUtils.toJsonPretty(resume);
        return json.getBytes();
    }
}
