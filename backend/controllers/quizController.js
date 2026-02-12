const db = require('../models');
const { Quiz, Submission, Student, User, Course, Teacher } = db;
const { Op } = require('sequelize');

/**
 * HELPER: Calculates score based on quiz rules
 */
const calculateScore = (quiz, studentAnswers) => {
    let score = 0;
    studentAnswers.forEach(ans => {
        const question = quiz.questions.find(q => q.id === ans.questionId);
        if (!question) return;

        const isCorrect = ans.selectedOption === question.options[question.correctOptionIndex];
        if (isCorrect) {
            score += (question.mark || 0);
        } else if (quiz.negativeMarking > 0) {
            score -= quiz.negativeMarking;
        }
    });
    return score;
};

// --- CONTROLLERS ---

exports.createQuiz = async (req, res) => {
    try {
        const { title, timeLimit, negativeMarking, totalMarks, questions, courseId } = req.body;
        if ([title, timeLimit, questions, totalMarks].some(f => f === undefined)) {
            return res.status(400).json({ error: 'Missing required fields' });
        }
        const processedQuestions = questions.map((q, idx) => ({ ...q, id: q.id || idx + 1 }));
        const quiz = await Quiz.create({
            title, timeLimit, negativeMarking, totalMarks,
            questions: processedQuestions,
            createdBy: req.user.id,
            courseId: courseId || null
        });
        res.status(201).json({ message: 'Quiz created successfully', quiz });
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getQuizzes = async (req, res) => {
    try {
        const quizzes = await Quiz.findAll({
            include: [{ model: User, as: 'creator', attributes: ['firstName', 'lastName'] }]
        });
        res.status(200).json(quizzes);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getQuizById = async (req, res) => {
    try {
        const quiz = await Quiz.findByPk(req.params.quizId);
        if (!quiz) return res.status(404).json({ error: 'Quiz not found' });
        res.status(200).json(quiz);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.updateQuiz = async (req, res) => {
    try {
        const quiz = await Quiz.findByPk(req.params.quizId);
        if (!quiz) return res.status(404).json({ error: 'Quiz not found' });
        await quiz.update(req.body);
        res.status(200).json({ message: 'Updated', quiz });
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.deleteQuiz = async (req, res) => {
    try {
        const quiz = await Quiz.findByPk(req.params.quizId);
        if (!quiz) return res.status(404).json({ error: 'Quiz not found' });
        await quiz.destroy();
        res.status(200).json({ message: 'Deleted' });
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.submitQuiz = async (req, res) => {
    try {
        const { studentId, quizId, answers } = req.body;
        const quiz = await Quiz.findByPk(quizId);
        const score = calculateScore(quiz, answers);
        const submission = await Submission.create({ studentId, quizId, answers, score, submittedAt: new Date() });
        res.status(201).json(submission);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getStudentReport = async (req, res) => {
    try {
        const submission = await Submission.findOne({
            where: { quizId: req.params.quizId, studentId: req.params.studentId },
            include: [{ model: Quiz, as: 'quiz' }]
        });
        if (!submission) return res.status(404).json({ error: 'Report not found' });
        res.status(200).json(submission);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getQuizzesForStudents = async (req, res) => {
    try {
        const student = await Student.findOne({ where: { userId: req.user.id } });
        const enrolledCourses = await student.getCourses();
        const quizzes = await Quiz.findAll({ where: { courseId: { [Op.in]: enrolledCourses.map(c => c.id) } } });
        res.status(200).json(quizzes);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getSubmissions = async (req, res) => {
    try {
        const submissions = await Submission.findAll({ where: { quizId: req.params.quizId } });
        res.status(200).json(submissions);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

exports.getAllStudentSubmissions = async (req, res) => {
    try {
        const submissions = await Submission.findAll({ where: { studentId: req.params.studentId } });
        res.status(200).json(submissions);
    } catch (error) { res.status(500).json({ error: error.message }); }
};

// Clean Export
module.exports = exports;