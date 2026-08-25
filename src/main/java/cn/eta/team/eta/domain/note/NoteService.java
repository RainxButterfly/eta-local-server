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

/**
 * 笔记业务逻辑层。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
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

        Page<Note> notePage = noteRepository.findByOwnerIdWithFilters(
                ownerId, q.keyword(), q.tag(), pageable);

        return Paged.of(notePage);
    }

    @Transactional
    public Note create(String ownerId, CreateRequest q) {
        Note note = new Note();
        note.setOwnerId(ownerId);
        note.setTitle(q.title());
        note.setBody(q.body());
        note.setTags(resolveTags(ownerId, q.tagNames()));
        return noteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public Note detail(String ownerId, String noteId) {
        return requireOwnedNote(noteId, ownerId);
    }

    @Transactional
    public Note update(String ownerId, String noteId, UpdateRequest q) {
        Note note = requireOwnedNote(noteId, ownerId);
        if (q.title() != null && !q.title().isBlank()) {
            note.setTitle(q.title());
        }
        if (q.body() != null) {
            note.setBody(q.body());
        }
        if (q.tagNames() != null) {
            note.setTags(resolveTags(ownerId, q.tagNames()));
        }
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    @Transactional
    public void remove(String ownerId, String id) {
        Note note = requireOwnedNote(id, ownerId);
        noteRepository.delete(note);
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
        if (noteTagRepository.existsByOwnerIdAndName(ownerId, q.name())) {
            throw new BizException(ErrorCode.NOTE_TAG_NAME_EXISTS);
        }
        NoteTag tag = new NoteTag();
        tag.setOwnerId(ownerId);
        tag.setName(q.name());
        tag.setColor(q.color());
        return noteTagRepository.save(tag);
    }

    @Transactional
    public void removeTag(String ownerId, String tagId) {
        NoteTag tag = noteTagRepository.findById(tagId)
                .orElseThrow(() -> new BizException(ErrorCode.NOTE_TAG_NOT_FOUND));
        if (!tag.getOwnerId().equals(ownerId)) {
            throw new BizException(ErrorCode.NOTE_TAG_NOT_FOUND);
        }
        // 先从所有引用该标签的笔记中解绑
        List<Note> notes = noteRepository.findByOwnerIdAndTagsId(ownerId, tagId);
        for (Note note : notes) {
            note.getTags().remove(tag);
        }
        noteRepository.saveAll(notes);
        noteTagRepository.delete(tag);
    }

    // ---------- 私有辅助 ----------

    private Note requireOwnedNote(String id, String ownerId) {
        return noteRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new BizException(ErrorCode.NOTE_NOT_FOUND));
    }

    /**
     * 将标签名列表解析为持久化的 NoteTag 实体列表：
     * 已存在的直接复用，不存在的新建并保存。空名自动跳过。
     */
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
            NoteTag tag = noteTagRepository.findByOwnerIdAndName(ownerId, trimmed)
                    .orElseGet(() -> {
                        NoteTag t = new NoteTag();
                        t.setOwnerId(ownerId);
                        t.setName(trimmed);
                        return noteTagRepository.save(t);
                    });
            resolved.add(tag);
        }
        return resolved;
    }
}
