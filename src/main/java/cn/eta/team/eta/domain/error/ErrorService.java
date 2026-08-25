// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.error;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.domain.error.ErrorDtos.CreateRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.QueryRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.ReviewSubmitRequest;
import cn.eta.team.eta.domain.error.ErrorDtos.ReviewSubmitVO;
import cn.eta.team.eta.domain.error.ErrorDtos.SubjectStat;
import cn.eta.team.eta.domain.error.ErrorDtos.UpdateRequest;
import io.github.openspacedrepetition.Card;
import io.github.openspacedrepetition.CardAndReviewLog;
import io.github.openspacedrepetition.Rating;
import io.github.openspacedrepetition.Scheduler;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ErrorService {
    private final ErrorRepository errorRepository;
    private final Scheduler fsrsScheduler;

    @Transactional(readOnly = true)
    public Paged<Error> list(QueryRequest q) {
        int pageNum = q.page() != null ? q.page() - 1 : 0;
        int page = PageUtils.page(pageNum);
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Error> errorPage;
        String subject = q.subject();
        if (subject != null && !subject.isBlank()) {
            errorPage = errorRepository.findBySubjectContaining(subject, pageable);
        } else {
            errorPage = errorRepository.findAll(pageable);
        }

        return Paged.of(errorPage);
    }

    @Transactional
    public Error create(CreateRequest q) {
        Error error = new Error();
        error.setQuestion(q.question());
        error.setAnswer(q.answer());
        error.setSubject(q.subject());
        error.setSource(q.source());
        error.setLevel(q.level());
        if (q.tags() != null && !q.tags().isEmpty()) {
            error.setTags(q.tags());
        }

        error.setDifficulty(0);
        error.setStability(0);
        error.setLastReviewAt(null);
        error.setNextReviewAt(Instant.now());
        return errorRepository.save(error);
    }

    @Transactional(readOnly = true)
    public Error detail(String id) {
        return requireError(id);
    }

    private Error requireError(String id) {
        return errorRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ERROR_NOT_FOUND));
    }

    @Transactional
    public Error update(String id, UpdateRequest q) {
        Error error = requireError(id);

        if (q.question() != null) {
            error.setQuestion(q.question());
        }
        if (q.answer() != null) {
            error.setAnswer(q.answer());
        }
        if (q.tags() != null) {
            error.setTags(q.tags());
        }

        return errorRepository.save(error);
    }

    @Transactional
    public void remove(String id) {
        Error error = requireError(id);
        errorRepository.delete(error);
    }

    @Transactional(readOnly = true)
    public List<SubjectStat> getSubjectStats() {
        List<Object[]> results = errorRepository.countBySubject();
        return results.stream()
                .map(row -> new SubjectStat((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());
    }

    public List<Error> review() {
        return errorRepository.findByNextReviewAtLessThanEqual(Instant.now());
    }

    @Transactional
    public ReviewSubmitVO submitReview(String errorId, ReviewSubmitRequest q) {
        Error error = requireError(errorId);

        boolean remembered = Boolean.TRUE.equals(q.remembered());

        Card oldCard = Card.builder()
                .difficulty(error.getDifficulty())
                .stability(error.getStability())
                .lastReview(error.getLastReviewAt() != null
                        ? error.getLastReviewAt()
                        : error.getCreatedAt())
                .due(error.getNextReviewAt())
                .build();

        Rating rating = remembered ? Rating.EASY : Rating.HARD;

        CardAndReviewLog result = fsrsScheduler.reviewCard(oldCard, rating);
        Card newCard = result.card();

        error.setDifficulty(newCard.getDifficulty());
        error.setStability(newCard.getStability());
        error.setLastReviewAt(Instant.now());
        error.setNextReviewAt(newCard.getDue());
        error.setMastered(newCard.getStability() > 365.0);
        error.setWrongCount(remembered ? error.getWrongCount() : error.getWrongCount() + 1);
        error.setLastWrongAt(remembered ? error.getLastWrongAt() : Instant.now());

        errorRepository.save(error);

        return new ReviewSubmitVO(errorId, error.isMastered(), error.getWrongCount());
    }
}
