// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.error;

import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.error.ErrorDtos.CreateRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.QueryRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.ReviewSubmitRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.ReviewSubmitVO;
import cn.eta.team.eta.domain.error.ErrorDtos.SubjectStat;
import cn.eta.team.eta.domain.error.ErrorDtos.UpdateRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 错题本
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/errors")
@RequiredArgsConstructor
public class ErrorbookController {

    private final ErrorService errorService;
    @GetMapping
    public Result<Paged<Error>> list(@AuthenticationPrincipal EtaPrincipal p, @ModelAttribute QueryRequest q) {
        return Result.ok(errorService.list(p.userId(), q));
    }

    @PostMapping
    public Result<Error> create(@AuthenticationPrincipal EtaPrincipal p, @RequestBody CreateRequest q) {
        return Result.ok(errorService.create(p.userId() ,q));
    }

    @GetMapping("/{id}")
    public Result<Error> detail(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        return Result.ok(errorService.detail(p.userId(), id));
    }

    @PutMapping("/{id}")
    public Result<Error> update(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id, @RequestBody UpdateRequest q) {
        return Result.ok(errorService.update(p.userId(), id, q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        errorService.remove(p.userId(), id);
        return Result.ok();
    }

    /** 学科分布统计 */
    @GetMapping("/categories")
    public Result<List<SubjectStat>> categories(@AuthenticationPrincipal EtaPrincipal p) {
        return Result.ok(errorService.getSubjectStats(p.userId()));
    }

    /** 今日应复习错题 */
    @GetMapping("/review")
    public Result<List<Error>> review(@AuthenticationPrincipal EtaPrincipal p) {
        return Result.ok(errorService.review(p.userId()));
    }

    /** 提交复习结果 */
    @PostMapping("/{id}/review")
    public Result<ReviewSubmitVO> submitReview(@AuthenticationPrincipal EtaPrincipal p,@PathVariable String id,@RequestBody ReviewSubmitRequest q) {
        return Result.ok(errorService.submitReview(p.userId(), id, q));                                         
    }
}