const express = require('express');
const router = express.Router();
const discussionController = require('../controllers/discussionController');
const authMiddleware = require('../middleware/authMiddleware');

router.get('/courses/:courseId/comments', discussionController.getCourseComments);
router.post('/comments', authMiddleware, discussionController.createComment);
router.delete('/comments/:id', authMiddleware, discussionController.deleteComment);
router.put('/comments/:id', authMiddleware, discussionController.updateComment);

module.exports = router;