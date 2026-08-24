package cn.eta.team.eta.domain.resume;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceItem {
    @NotBlank(message = "经历标题不能为空")
    private String title;
    private String subtitle;
    private String period;
}