package cn.eta.team.eta.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 简历模块 DTO。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
public final class ResumeDtos{

    private ResumeDtos() {
    }

    /** 简历创建入参 */
    public record CreateRequest(
        @NotBlank(message = "简历标题不能为空")
        String name,
        @NotBlank(message = "简历模板ID不能为空")
        String templateId,
        @NotNull(message = "简历内容不能为空")
        ResumeContent content
    ) {
    }

    public record QueryRequest(Integer page, Integer pageSize) {}
}