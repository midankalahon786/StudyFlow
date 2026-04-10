package com.r786.studyflow.modules.discussion.repository;

import com.r786.studyflow.modules.discussion.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Fetch only top-level comments for a thread (where parent_comment_id is NULL)
    // JPA will handle the nested 'replies' collection automatically
    List<Comment> findByThreadIdAndParentCommentIsNullOrderByCreatedAtAsc(Long threadId);

    // Count total interactions on a thread
    long countByThreadId(Long threadId);

    // Optimized join to avoid N+1 issues when viewing a discussion
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies " +
            "JOIN FETCH c.author " +
            "WHERE c.thread.id = :threadId AND c.parentComment IS NULL")
    List<Comment> findThreadTree(Long threadId);
}
