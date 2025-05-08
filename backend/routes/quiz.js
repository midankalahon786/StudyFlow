const express = require('express');
const router = express.Router();
const quizController = require('../controllers/quizController');
const authenticateToken = require('../middleware/authMiddleware');

// Teacher routes
router.post('/create', authenticateToken, quizController.createQuiz);
router.get('/', authenticateToken, quizController.getQuizzes);

// Student routes
router.get('/student-quizzes', authenticateToken, quizController.getQuizzesForStudents);
router.post('/submit', authenticateToken, quizController.submitQuiz);
router.get('/report/:studentId', authenticateToken, quizController.getStudentReport);

// Teacher can see submissions
router.get('/submissions/:quizId', authenticateToken, quizController.getSubmissions);

module.exports = router;
