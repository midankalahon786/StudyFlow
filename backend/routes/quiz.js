const express = require('express');
const router = express.Router();
const quizController = require('../controllers/quizController');
const authenticateToken = require('../middleware/authMiddleware');

// Teacher routes
router.post('/create', authenticateToken, quizController.createQuiz);
router.get('/list', authenticateToken, quizController.getQuizzes);
router.delete('/:quizId', authenticateToken, quizController.deleteQuiz);
router.get('/student-quizzes', authenticateToken, quizController.getQuizzesForStudents);
router.get('/:quizId', authenticateToken, quizController.getQuizById); 
router.put('/:quizId', authenticateToken, quizController.updateQuiz);

// Student routes

router.post('/submit', authenticateToken, quizController.submitQuiz);
router.get('/:quizId/report/:studentId', authenticateToken, quizController.getStudentReport);

router.get('/student/:studentId/submissions', authenticateToken, quizController.getAllStudentSubmissions);

router.get('/submissions/:quizId', authenticateToken, quizController.getSubmissions);

module.exports = router;