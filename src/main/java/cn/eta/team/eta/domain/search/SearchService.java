// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.domain.error.Error;
import cn.eta.team.eta.domain.error.ErrorRepository;
import cn.eta.team.eta.domain.note.Note;
import cn.eta.team.eta.domain.note.NoteRepository;
import cn.eta.team.eta.domain.search.SearchDtos.SearchGroup;
import cn.eta.team.eta.domain.search.SearchDtos.SearchItem;
import cn.eta.team.eta.domain.search.SearchDtos.SearchResponse;
import cn.eta.team.eta.domain.task.Task;
import cn.eta.team.eta.domain.task.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int SUBTITLE_LEN = 100;

    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final ErrorRepository errorRepository;

    @Transactional(readOnly = true)
    public SearchResponse search(String ownerId, String keyword, Integer page, Integer pageSize) {
        int p = PageUtils.page(page != null ? page - 1 : 0);
        int size = PageUtils.size(pageSize);
        Pageable pageable = PageRequest.of(p, size);

        List<SearchGroup> groups = new ArrayList<>();
        long total = 0;

        Page<Task> tasks = taskRepository.findWithFilters(ownerId, null, null, keyword, pageable);
        groups.add(new SearchGroup("task", "任务",
                tasks.getContent().stream().map(this::toTaskItem).toList()));
        total += tasks.getTotalElements();

        Page<Note> notes = noteRepository.findWithFilters(ownerId, keyword, null, pageable);
        groups.add(new SearchGroup("note", "笔记",
                notes.getContent().stream().map(this::toNoteItem).toList()));
        total += notes.getTotalElements();

        Page<Error> errors = errorRepository.findWithFilters(ownerId, keyword, pageable);
        groups.add(new SearchGroup("error", "错题",
                errors.getContent().stream().map(this::toErrorItem).toList()));
        total += errors.getTotalElements();

        return new SearchResponse(keyword, total, groups);
    }

    private SearchItem toTaskItem(Task t) {
        return new SearchItem(t.getId(), "task", t.getTitle(), truncate(t.getDescription()), t.getTone());
    }

    private SearchItem toNoteItem(Note n) {
        return new SearchItem(n.getId(), "note", n.getTitle(), truncate(n.getBody()), n.getTone());
    }

    private SearchItem toErrorItem(Error e) {
        return new SearchItem(e.getId(), "error", truncate(e.getQuestion()), truncate(e.getAnswer()), e.getTone());
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() > SUBTITLE_LEN ? s.substring(0, SUBTITLE_LEN) + "..." : s;
    }
}
