package com.r786.studyflow.modules.discussion.repository;

import com.r786.studyflow.modules.discussion.entity.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<DiscussionThread, Long> {

    // Fetch all threads for a specific course
    List<DiscussionThread> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    // Optimized fetch to get the author details immediately
    @Query("SELECT t FROM DiscussionThread t JOIN FETCH t.author WHERE t.course.id = :courseId")
    List<DiscussionThread> findAllByCourseIdWithAuthor(Long courseId);
}