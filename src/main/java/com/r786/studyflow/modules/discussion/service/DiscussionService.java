package com.r786.studyflow.modules.discussion.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.modules.auth.repository.UserRepository;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.discussion.controller.DiscussionController;
import com.r786.studyflow.modules.discussion.entity.Comment;
import com.r786.studyflow.modules.discussion.entity.DiscussionThread;
import com.r786.studyflow.modules.discussion.repository.CommentRepository;
import com.r786.studyflow.modules.discussion.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment addComment(Long threadId, Long authorId, String content, Long parentId) {
        var thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Thread not found"));

        Comment comment = Comment.builder()
                .thread(thread)
                .content(content)
                // Author logic matches your User entity
                .build();

        if (parentId != null) {
            var parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public DiscussionThread createThread(DiscussionController.ThreadRequest request) {
        var course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found with ID: " + request.courseId()));

        var author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("User not found with ID: " + request.authorId()));

        var thread = DiscussionThread.builder()
                .course(course)
                .author(author)
                .title(request.title())
                .content(request.content())
                .build();

        return threadRepository.save(thread);
    }

    @Transactional(readOnly = true)
    public List<Comment> getThreadTree(Long threadId) {
        // First, verify the thread exists
        if (!threadRepository.existsById(threadId)) {
            throw new GlobalExceptionHandler.ResourceNotFoundException("Thread not found with ID: " + threadId);
        }

        // Fetch top-level comments. JPA will handle the nested 'replies' tree.
        return commentRepository.findByThreadIdAndParentCommentIsNullOrderByCreatedAtAsc(threadId);
    }
}