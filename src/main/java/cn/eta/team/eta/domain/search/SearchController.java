// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.search;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.search.SearchDtos.SearchResponse;
import cn.eta.team.eta.security.EtaPrincipal;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索控制器
 * @author ormisnal
 * @since 2026-08-27
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result<SearchResponse> search(@AuthenticationPrincipal EtaPrincipal principal,
                                          @RequestParam String keyword,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer pageSize) {
        if (keyword == null || keyword.isBlank()) {
            throw new BizException(ErrorCode.SEARCH_KEYWORD_EMPTY);
        }
        return Result.ok(searchService.search(principal.userId(), keyword.trim(), page, pageSize));
    }
}
