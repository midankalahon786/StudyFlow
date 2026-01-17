const { Comment, User, Course } = require('../models'); 

exports.getCourseComments = async (req, res) => {
    try {
        const courseId = req.params.courseId;

        if (!courseId) {
            return res.status(400).json({ message: 'Course ID is required.' });
        }
        const comments = await Comment.findAll({
            where: {
                courseId: courseId,
                parentId: null 
            },
            include: [
                {
                    model: User,
                    as: 'author', 
                    attributes: ['id', 'username', 'email'] 
                },
                {
                    model: Comment, 
                    as: 'replies',
                    include: [{
                        model: User,
                        as: 'author', 
                        attributes: ['id', 'username', 'email']
                    }],
                    order: [['createdAt', 'ASC']] 
                }
            ],
            order: [['createdAt', 'ASC']] 
        });
        res.status(200).json(comments);
    } catch (error) {
        console.error('Error fetching course comments:', error);
        res.status(500).json({ message: 'Error fetching comments', error: error.message });
    }
};

exports.createComment = async (req, res) => {
    try {
        const { content, courseId, parentId } = req.body;
        const userId = req.user.id;

        if (!content || typeof content !== 'string' || content.trim() === '') {
            return res.status(400).json({ message: 'Comment content is required and cannot be empty.' });
        }
        if (!courseId && !parentId) {
            return res.status(400).json({ message: 'Comment must be associated with a course or a parent comment.' });
        }

        let actualCourseId = null; 

        if (courseId) {
            const course = await Course.findByPk(courseId);
            if (!course) {
                return res.status(404).json({ message: 'Course not found.' });
            }
            actualCourseId = courseId;
        } else if (parentId) {
            const parentComment = await Comment.findByPk(parentId);
            if (!parentComment) {
                return res.status(404).json({ message: 'Parent comment not found.' });
            }
            actualCourseId = parentComment.courseId; 
        }

        if (!actualCourseId) {
             return res.status(400).json({ message: 'Could not determine associated course for comment.' });
        }

        const newComment = await Comment.create({
            content: content.trim(), 
            userId,
            courseId: actualCourseId,
            parentId: parentId || null 
        });

        const createdCommentWithAuthor = await Comment.findByPk(newComment.id, {
            include: [
                { model: User, as: 'author', attributes: ['id', 'username', 'email'] }
            ]
        });

        res.status(201).json(createdCommentWithAuthor); 
    } catch (error) {
        console.error('Error creating comment:', error);
        res.status(500).json({ message: 'Error creating comment', error: error.message });
    }
};

exports.deleteComment = async (req, res) => {
    try {
        const commentId = req.params.id;
        const userId = req.user.id;
        const userRole = req.user.role; 

        const comment = await Comment.findByPk(commentId);

        if (!comment) {
            return res.status(404).json({ message: 'Comment not found.' });
        }

        const isAuthor = comment.userId === userId;
        const isAdminOrTeacher = userRole === 'admin' || userRole === 'teacher';

        if (!isAuthor && !isAdminOrTeacher) {
            return res.status(403).json({ message: 'Unauthorized to delete this comment.' });
        }

        await comment.destroy(); 
        res.status(200).json({ message: 'Comment deleted successfully.' });
    } catch (error) {
        console.error('Error deleting comment:', error);
        res.status(500).json({ message: 'Error deleting comment', error: error.message });
    }
};

exports.updateComment = async (req, res) => {
    try {
        const commentId = req.params.id;
        const { content } = req.body;
        const userId = req.user.id;

        if (!content || typeof content !== 'string' || content.trim() === '') {
            return res.status(400).json({ message: 'Valid comment content is required for updating.' });
        }

        const comment = await Comment.findByPk(commentId);

        if (!comment) {
            return res.status(404).json({ message: 'Comment not found.' });
        }

        if (comment.userId !== userId) {
            return res.status(403).json({ message: 'Unauthorized to update this comment.' });
        }

        comment.content = content.trim(); 
        await comment.save(); 

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