// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.domain.note;

import cn.eta.team.eta.common.Paged;
import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.domain.note.NoteDtos.CreateRequest;
import cn.eta.team.eta.domain.note.NoteDtos.CreateTagRequest;
import cn.eta.team.eta.domain.note.NoteDtos.QueryRequest;
import cn.eta.team.eta.domain.note.NoteDtos.TagStat;
import cn.eta.team.eta.domain.note.NoteDtos.UpdateRequest;
import cn.eta.team.eta.security.EtaPrincipal;
import jakarta.validation.Valid;
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
 * 笔记接口。
 *
 * @author StarLeaf-Roxy ormisnal
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public Result<Paged<Note>> list(@AuthenticationPrincipal EtaPrincipal p, @ModelAttribute QueryRequest q) {
        return Result.ok(noteService.list(p.userId(), q));
    }

    @PostMapping
    public Result<Note> create(@AuthenticationPrincipal EtaPrincipal p, @Valid @RequestBody CreateRequest q) {
        return Result.ok(noteService.create(p.userId(), q));
    }

    @GetMapping("/{id}")
    public Result<Note> detail(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        return Result.ok(noteService.detail(p.userId(), id));
    }

    @PutMapping("/{id}")
    public Result<Note> update(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id,
                               @Valid @RequestBody UpdateRequest q) {
        return Result.ok(noteService.update(p.userId(), id, q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        noteService.remove(p.userId(), id);
        return Result.ok();
    }

    @GetMapping("/tags")
    public Result<List<TagStat>> listTags(@AuthenticationPrincipal EtaPrincipal p) {
        return Result.ok(noteService.listTags(p.userId()));
    }

    @PostMapping("/tags")
    public Result<NoteTag> createTag(@AuthenticationPrincipal EtaPrincipal p,
                                     @Valid @RequestBody CreateTagRequest q) {
        return Result.ok(noteService.createTag(p.userId(), q));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> removeTag(@AuthenticationPrincipal EtaPrincipal p, @PathVariable String id) {
        noteService.removeTag(p.userId(), id);
        return Result.ok();
    }
}
