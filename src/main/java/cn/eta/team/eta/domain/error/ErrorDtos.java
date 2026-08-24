package cn.eta.team.eta.domain.error;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 错题本模块 DTO。
 *
 * @author ormisnal
 * @since 2026-08-24
 */
public final class ErrorDtos {

    private ErrorDtos() {}

    /** 错题创建入参 */
    public record CreateRequest(
            @NotBlank(message = "题目不能为空")
            @Size(max = 2000, message = "题目太长")
            String question,

            @NotBlank(message = "答案不能为空")
            @Size(max = 2000, message = "答案太长")
            String answer,

            @NotBlank(message = "科目不能为空")
            @Size(max = 64, message = "科目名太长")
            String subject,

            @Size(max = 128, message = "来源太长")
            String source,

            @NotBlank(message = "难度不能为空")
            String level,

            @Size(max = 10, message = "标签最多 10 个")
            List<@NotBlank(message = "标签不能为空") String> tags
    ) {}

    /** 错题更新入参 */
    public record UpdateRequest(
            @NotBlank(message = "题目不能为空")
            @Size(max = 2000, message = "题目太长")
            String question,

            @NotBlank(message = "答案不能为空")
            @Size(max = 2000, message = "答案太长")
            String answer,

            @Size(max = 10, message = "标签最多 10 个")
            List<@NotBlank(message = "标签不能为空") String> tags
    ) {}

    /** 分页查询入参 */
    public record QueryRequest(
            Integer page,
            Integer pageSize,
            String subject
    ) {}
    
    /**
     * 学科统计结果（DTO）
     */
    public record SubjectStat(String subject, long count) {}
}