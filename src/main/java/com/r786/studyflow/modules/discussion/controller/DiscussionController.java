package com.r786.studyflow.modules.discussion.controller;

import com.r786.studyflow.modules.discussion.entity.Comment;
import com.r786.studyflow.modules.discussion.entity.DiscussionThread;
import com.r786.studyflow.modules.discussion.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussion")
@RequiredArgsConstructor
@Tag(name = "Discussion Module", description = "Endpoints for course-specific threads and nested comments")
public class DiscussionController {

    private final DiscussionService discussionService;

    @Operation(summary = "Create a new thread", description = "Teachers or Students can start a topic in a course.")
    @PostMapping("/threads")
    public ResponseEntity<DiscussionThread> createThread(@RequestBody ThreadRequest request) {
        return ResponseEntity.ok(discussionService.createThread(request));
    }

    @Operation(summary = "Add a comment or reply", description = "Supports nested comments by providing a parentId.")
    @PostMapping("/comments")
    public ResponseEntity<Comment> addComment(
            @RequestParam Long threadId,
            @RequestParam Long authorId,
            @RequestParam(required = false) Long parentId,
            @RequestBody String content) {
        return ResponseEntity.ok(discussionService.addComment(threadId, authorId, content, parentId));
    }

    @Operation(summary = "Get discussion tree", description = "Fetches the thread and all its nested replies.")
    @GetMapping("/threads/{id}")
    public ResponseEntity<List<Comment>> getDiscussionTree(@PathVariable Long id) {
        return ResponseEntity.ok(discussionService.getThreadTree(id));
    }

    public record ThreadRequest(Long courseId, Long authorId, String title, String content) {}
}
