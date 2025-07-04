// backend/routes/discussion.js
const express = require('express');
const router = express.Router();
const discussionController = require('../controllers/discussionController');
const authMiddleware = require('../middleware/authMiddleware'); // Your existing authentication middleware

// Route to get all comments for a specific course
// GET /api/discussion/courses/:courseId/comments
router.get('/courses/:courseId/comments', discussionController.getCourseComments);

// Route to create a new comment (requires authentication)
// POST /api/discussion/comments
router.post('/comments', authMiddleware, discussionController.createComment);

// Route to delete a comment (requires authentication and authorization)
// DELETE /api/discussion/comments/:id
router.delete('/comments/:id', authMiddleware, discussionController.deleteComment);

// Route to update a comment (requires authentication and authorization)
// PUT /api/discussion/comments/:id
router.put('/comments/:id', authMiddleware, discussionController.updateComment);

module.exports = router;