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
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public Result<Paged<Note>> list(@ModelAttribute QueryRequest q) {
        return Result.ok(noteService.list(q));
    }

    @PostMapping
    public Result<Note> create(@Valid @RequestBody CreateRequest q) {
        return Result.ok(noteService.create(q));
    }

    @GetMapping("/{id}")
    public Result<Note> detail(@PathVariable String id) {
        return Result.ok(noteService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Note> update(@PathVariable String id, @Valid @RequestBody UpdateRequest q) {
        return Result.ok(noteService.update(id, q));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable String id) {
        noteService.remove(id);
        return Result.ok();
    }

    @GetMapping("/tags")
    public Result<List<TagStat>> listTags() {
        return Result.ok(noteService.listTags());
    }

    @PostMapping("/tags")
    public Result<NoteTag> createTag(@Valid @RequestBody CreateTagRequest q) {
        return Result.ok(noteService.createTag(q));
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> removeTag(@PathVariable String id) {
        noteService.removeTag(id);
        return Result.ok();
    }
}
