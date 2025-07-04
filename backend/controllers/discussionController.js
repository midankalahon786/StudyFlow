// backend/controllers/discussionController.js
const { Comment, User, Course } = require('../models'); 

exports.getCourseComments = async (req, res) => {
    try {
        const courseId = req.params.courseId;

        // Basic validation for courseId to ensure it's provided
        if (!courseId) {
            return res.status(400).json({ message: 'Course ID is required.' });
        }

        // Find all comments for the given courseId.
        // We fetch only top-level comments (parentId is null) and then eagerly load their replies.
        const comments = await Comment.findAll({
            where: {
                courseId: courseId,
                parentId: null // Fetch only top-level comments initially
            },
            include: [
                {
                    model: User,
                    as: 'author', // This alias matches the 'as' in Comment.belongsTo(User) in models/index.js
                    attributes: ['id', 'username', 'email'] // Select only necessary user attributes for the author
                },
                {
                    model: Comment, // Recursively include replies
                    as: 'replies', // This alias matches the 'as' in Comment.hasMany(Comment) in models/index.js
                    include: [{
                        model: User,
                        as: 'author', // Include author for replies as well
                        attributes: ['id', 'username', 'email']
                    }],
                    order: [['createdAt', 'ASC']] // Order replies chronologically
                }
            ],
            order: [['createdAt', 'ASC']] // Order top-level comments chronologically
        });
        res.status(200).json(comments);
    } catch (error) {
        console.error('Error fetching course comments:', error);
        res.status(500).json({ message: 'Error fetching comments', error: error.message });
    }
};

// @route POST /api/discussion/comments
// @desc Create a new comment or reply
// @access Private (requires authentication via authMiddleware)
exports.createComment = async (req, res) => {
    try {
        const { content, courseId, parentId } = req.body;
        // Assuming req.user is populated by your authMiddleware with the authenticated user's ID
        const userId = req.user.id;

        // Basic validation for request body
        if (!content || typeof content !== 'string' || content.trim() === '') {
            return res.status(400).json({ message: 'Comment content is required and cannot be empty.' });
        }
        // A comment must either belong to a course or be a reply to an existing comment
        if (!courseId && !parentId) {
            return res.status(400).json({ message: 'Comment must be associated with a course or a parent comment.' });
        }

        let actualCourseId = null; // This will hold the final courseId for the comment

        if (courseId) {
            // If courseId is provided, validate its existence
            const course = await Course.findByPk(courseId);
            if (!course) {
                return res.status(404).json({ message: 'Course not found.' });
            }
            actualCourseId = courseId;
        } else if (parentId) {
            // If parentId is provided (it's a reply), fetch the parent comment
            // to inherit its courseId and validate parent existence
            const parentComment = await Comment.findByPk(parentId);
            if (!parentComment) {
                return res.status(404).json({ message: 'Parent comment not found.' });
            }
            actualCourseId = parentComment.courseId; // Inherit courseId from the parent comment
        }

        // If for some reason actualCourseId is still null, it's an invalid request
        if (!actualCourseId) {
             return res.status(400).json({ message: 'Could not determine associated course for comment.' });
        }

        // Create the new comment in the database
        const newComment = await Comment.create({
            content: content.trim(), // Trim whitespace from content
            userId,
            courseId: actualCourseId,
            parentId: parentId || null // Set parentId if it's a reply, otherwise null for top-level comments
        });

        // Fetch the newly created comment along with its author's details.
        // This is useful for the frontend to immediately display the comment with complete user information
        // without making a separate request.
        const createdCommentWithAuthor = await Comment.findByPk(newComment.id, {
            include: [
                { model: User, as: 'author', attributes: ['id', 'username', 'email'] }
            ]
        });

        res.status(201).json(createdCommentWithAuthor); // Respond with the full comment object
    } catch (error) {
        console.error('Error creating comment:', error);
        res.status(500).json({ message: 'Error creating comment', error: error.message });
    }
};

// @route DELETE /api/discussion/comments/:id
// @desc Delete a comment
// @access Private (requires authentication and authorization)
exports.deleteComment = async (req, res) => {
    try {
        const commentId = req.params.id;
        // Assuming req.user is populated by your authMiddleware with authenticated user's ID and role
        const userId = req.user.id;
        const userRole = req.user.role; // e.g., 'student', 'teacher', 'admin'

        const comment = await Comment.findByPk(commentId);

        if (!comment) {
            return res.status(404).json({ message: 'Comment not found.' });
        }

        // Authorization logic:
        // A comment can be deleted by its original author, or by a teacher, or by an admin.
        const isAuthor = comment.userId === userId;
        const isAdminOrTeacher = userRole === 'admin' || userRole === 'teacher';

        if (!isAuthor && !isAdminOrTeacher) {
            return res.status(403).json({ message: 'Unauthorized to delete this comment.' });
        }

        await comment.destroy(); // Delete the comment from the database
        res.status(200).json({ message: 'Comment deleted successfully.' });
    } catch (error) {
        console.error('Error deleting comment:', error);
        res.status(500).json({ message: 'Error deleting comment', error: error.message });
    }
};

// @route PUT /api/discussion/comments/:id
// @desc Update a comment
// @access Private (requires authentication and authorization - only by author)
exports.updateComment = async (req, res) => {
    try {
        const commentId = req.params.id;
        const { content } = req.body;
        // Assuming req.user is populated by your authMiddleware with the authenticated user's ID
        const userId = req.user.id;

        // Basic validation for new content
        if (!content || typeof content !== 'string' || content.trim() === '') {
            return res.status(400).json({ message: 'Valid comment content is required for updating.' });
        }

        const comment = await Comment.findByPk(commentId);

        if (!comment) {
            return res.status(404).json({ message: 'Comment not found.' });
        }

        // Authorization: Only the original author can update their comment.
        if (comment.userId !== userId) {
            return res.status(403).json({ message: 'Unauthorized to update this comment.' });
        }

        comment.content = content.trim(); // Update the content, trim whitespace
        await comment.save(); // Save the changes to the database

        // Optionally, fetch the updated comment with author details for immediate UI refresh
        const updatedCommentWithAuthor = await Comment.findByPk(comment.id, {
            include: [
                { model: User, as: 'author', attributes: ['id', 'username', 'email'] }
            ]
        });

        res.status(200).json({ message: 'Comment updated successfully.', comment: updatedCommentWithAuthor });
    } catch (error) {
        console.error('Error updating comment:', error);
        res.status(500).json({ message: 'Error updating comment', error: error.message });
    }
};