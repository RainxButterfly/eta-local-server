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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

@RestController
@RequestMapping("/errors")
@RequiredArgsConstructor
public class ErrorbookController {

    private final ErrorService errorService;

    @GetMapping
    public Result<Paged<Error>> list(@ModelAttribute QueryRequest q) {
        return Result.ok(errorService.list(q));
    }

    @PostMapping
    public Result<Error> create(@Valid @RequestBody CreateRequest q) {
        return Result.ok(errorService.create(q));
    }

    @GetMapping("/{id}")
    public Result<Error> detail(@PathVariable String id) {
        return Result.ok(errorService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Error> update(@PathVariable String id, @Valid @RequestBody UpdateRequest q) {
        return Result.ok(errorService.update(id, q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        errorService.remove(id);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<SubjectStat>> categories() {
        return Result.ok(errorService.getSubjectStats());
    }

    @GetMapping("/review")
    public Result<List<Error>> review() {
        return Result.ok(errorService.review());
    }

    @PostMapping("/{id}/review")
    public Result<ReviewSubmitVO> submitReview(@PathVariable String id, @Valid @RequestBody ReviewSubmitRequest q) {
        return Result.ok(errorService.submitReview(id, q));
    }
}
