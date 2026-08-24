package cn.eta.team.eta.resume;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumeContent {
    private String name;
    private String role;
    private String email;
    private String phone;

    // 教育经历列表
    @ElementCollection
    @CollectionTable(name = "resume_education", joinColumns = @JoinColumn(name = "resume_id"))
    @OrderColumn(name = "sort_order")
    private List<ExperienceItem> education = new ArrayList<>();

    // 工作经历列表
    @ElementCollection
    @CollectionTable(name = "resume_experience", joinColumns = @JoinColumn(name = "resume_id"))
    @OrderColumn(name = "sort_order")
    private List<ExperienceItem> experience = new ArrayList<>();

    // 项目经历列表
    @ElementCollection
    @CollectionTable(name = "resume_projects", joinColumns = @JoinColumn(name = "resume_id"))
    @OrderColumn(name = "sort_order")
    private List<ExperienceItem> projects = new ArrayList<>();

    // 技能列表
    @ElementCollection
    @CollectionTable(name = "resume_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();
}