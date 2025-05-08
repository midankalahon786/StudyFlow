const Quiz = require('../models/quiz');
const Submission = require('../models/submissions');
const { Op } = require('sequelize');

// Create a new quiz
exports.createQuiz = async (req, res) => {
    const { title, timeLimit, negativeMarking, totalMarks, questions } = req.body;
    try {
        const quiz = await Quiz.create({
            title,
            timeLimit,
            negativeMarking,
            totalMarks,
            questions,
        });
        res.status(201).json(quiz);
    } catch (err) {
        console.error("Quiz creation error:", err);
        res.status(500).json({ error: 'Failed to create quiz', message: err.message });

    }
};

// Get all quizzes for the teacher to see submissions
exports.getQuizzes = async (req, res) => {
    try {
        const quizzes = await Quiz.findAll();
        res.status(200).json(quizzes);
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch quizzes', message: err.message });
    }
};

// Get submissions for a specific quiz
exports.getSubmissions = async (req, res) => {
    const { quizId } = req.params;
    try {
        const submissions = await Submission.findAll({
            where: { quizId },
            include: ['student'], // assuming you have a student relationship
        });
        res.status(200).json(submissions);
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch submissions', message: err.message });
    }
};

// Student submits answers for a quiz
exports.submitQuiz = async (req, res) => {
    const { studentId, quizId, answers } = req.body;

    // Calculate score based on answers and quiz criteria
    try {
        const quiz = await Quiz.findByPk(quizId);
        if (!quiz) return res.status(404).json({ error: 'Quiz not found' });

        let score = 0;
        // Loop over questions to calculate the score
        quiz.questions.forEach((question, index) => {
            if (answers[index] === question.correctAnswer) {
                score += question.mark;
            } else {
                score -= quiz.negativeMarking;
            }
        });

        // Save submission
        const submission = await Submission.create({
            studentId,
            quizId,
            answers,
            score,
        });

        res.status(201).json(submission);
    } catch (err) {
        res.status(500).json({ error: 'Failed to submit quiz', message: err.message });
    }
};

// Get report of student's submission
exports.getStudentReport = async (req, res) => {
    const { studentId } = req.params;
    try {
        const submissions = await Submission.findAll({
            where: { studentId },
            include: ['quiz'],  // assuming you have a quiz relationship
        });
        res.status(200).json(submissions);
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch student report', message: err.message });
    }
};
exports.getQuizzesForStudents = async (req, res) => {
    try {
        const quizzes = await Quiz.findAll({
            attributes: ['id', 'title', 'timeLimit', 'totalMarks', 'createdAt', 'updatedAt'] // no answers
        });
        res.status(200).json(quizzes);
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch quizzes for students', message: err.message });
    }
};
