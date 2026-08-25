package cn.eta.team.eta.domain.resume;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 简历模块 DTO。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
public final class ResumeDtos {

        private ResumeDtos() {
        }

        /** 简历创建入参 */
        public record CreateRequest(
                        @NotBlank(message = "简历标题不能为空") String name,
                        @NotBlank(message = "简历模板ID不能为空") String templateId,
                        @Valid ResumeContent content) {
        }

        /** 简历更新入参 */
        public record UpdateRequest(
                        String name,
                        @Valid ResumeContent content) {
        }

        /** 分页查询入参 */
        public record QueryRequest(
                        Integer page,
                        Integer pageSize) {
        }
}