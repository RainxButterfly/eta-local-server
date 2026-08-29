// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.util.PageUtils;
import cn.eta.team.eta.domain.note.NoteDtos.CreateRequest;
import cn.eta.team.eta.domain.note.NoteDtos.CreateTagRequest;
import cn.eta.team.eta.domain.note.NoteDtos.QueryRequest;
import cn.eta.team.eta.domain.note.NoteDtos.TagStat;
import cn.eta.team.eta.domain.note.NoteDtos.UpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteTagRepository noteTagRepository;

    @Transactional(readOnly = true)
    public Paged<Note> list(String ownerId, QueryRequest q) {
        int pageNum = q.page() != null ? q.page() - 1 : 0;
        int page = PageUtils.page(pageNum);
        int size = PageUtils.size(q.pageSize());
        Pageable pageable = PageRequest.of(page, size);

        Page<Note> notePage = noteRepository.findWithFilters(ownerId, q.keyword(), q.tag(), pageable);
        return Paged.of(notePage);
    }

    @Transactional
    public Note create(String ownerId, CreateRequest q) {
        Note note = new Note();
        note.setTitle(q.title());
        note.setBody(q.body());
        note.setOwnerId(ownerId);
        note.setTags(resolveTags(ownerId, q.tagNames()));
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public Note detail(String noteId) {
        return requireNote(noteId);
    }

    @Transactional
    public Note update(String noteId, UpdateRequest q) {
        Note note = requireNote(noteId);
        if (q.title() != null && !q.title().isBlank()) {
            note.setTitle(q.title());
        }
        if (q.body() != null) {
            note.setBody(q.body());
        }
        if (q.tagNames() != null) {
            note.setTags(resolveTags(note.getOwnerId(), q.tagNames()));
        }
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    @Transactional
    public void remove(String id) {
        Note note = requireNote(id);
        note.setDeleted(true);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<TagStat> listTags(String ownerId) {
        List<Object[]> rows = noteRepository.listTags(ownerId);
        List<TagStat> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            String name = (String) row[0];
            Long count = (Long) row[1];
            result.add(new TagStat(name, count != null ? count.intValue() : 0));
        }
        return result;
    }

    @Transactional
    public NoteTag createTag(String ownerId, CreateTagRequest q) {
        if (noteTagRepository.existsByNameAndOwnerId(q.name(), ownerId)) {
            throw new BizException(ErrorCode.NOTE_TAG_NAME_EXISTS);
        }
        NoteTag tag = new NoteTag();
        tag.setName(q.name());
        tag.setColor(q.color());
        tag.setOwnerId(ownerId);
        return noteTagRepository.save(tag);
    }

    @Transactional
    public void removeTag(String ownerId, String tagId) {
        NoteTag tag = noteTagRepository.findById(tagId)
                .orElseThrow(() -> new BizException(ErrorCode.NOTE_TAG_NOT_FOUND));
        List<Note> notes = noteRepository.findByTagsIdAndOwnerId(tagId, ownerId);
        for (Note note : notes) {
            note.getTags().remove(tag);
            note.setUpdatedAt(Instant.now());
        }
        noteRepository.saveAll(notes);
        tag.setDeleted(true);
        tag.setUpdatedAt(Instant.now());
        noteTagRepository.save(tag);
    }

    private Note requireNote(String id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOTE_NOT_FOUND));
    }

    private List<NoteTag> resolveTags(String ownerId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<NoteTag> resolved = new ArrayList<>(tagNames.size());
        for (String name : tagNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            String trimmed = name.trim();
            NoteTag tag = noteTagRepository.findByNameAndOwnerId(trimmed, ownerId)
                    .orElseGet(() -> {
                        NoteTag t = new NoteTag();
                        t.setName(trimmed);
                        t.setOwnerId(ownerId);
                        return noteTagRepository.save(t);
                    });
            resolved.add(tag);
        }
        return resolved;
    }
}
