const db = require('../models'); 
const Course = db.Course;
const Quiz = db.Quiz; 
const Submission = db.Submission; 
const Student = db.Student; 
const User = db.User; 
const Teacher = db.Teacher; 

const { Op } = require('sequelize'); 

exports.createQuiz = async (req, res) => {
    console.log("Controller: Entered createQuiz function.");
    try {
        const { title, timeLimit, negativeMarking, totalMarks, questions, courseId } = req.body;
        const userId = req.user.id;
        const userRole = req.user.role;

        console.log("Create Quiz Request Body:", req.body);
        console.log("Title present:", !!title);
        console.log("timeLimit present:", !!timeLimit);
        console.log("negativeMarking present:", !!negativeMarking);
        console.log("totalMarks present:", !!totalMarks);
        console.log("questions present:", !!questions);

        if (typeof title === 'undefined' || typeof timeLimit === 'undefined' || typeof negativeMarking === 'undefined' || typeof totalMarks === 'undefined' || typeof questions === 'undefined') {
            return res.status(400).json({ error: 'All fields are required' });
        }

        if (userRole !== 'teacher' && userRole !== 'admin') {
            return res.status(403).json({ message: 'Only teachers or administrators can create quizzes.' });
        }

        if (courseId) {
            const course = await db.Course.findByPk(courseId);
            if (!course) {
                return res.status(404).json({ error: "Selected course not found" });
            }
        }

        let questionIdCounter = 1;
        const questionsWithUniqueIds = questions.map(q => {
            return {
                ...q,
                id: q.id || questionIdCounter++
            };
        });

        const quiz = await Quiz.create({
            title,
            timeLimit,
            negativeMarking,
            totalMarks,
            questions: questionsWithUniqueIds,
            createdBy: userId,
            courseId: courseId || null 
        });

        res.status(201).json({ message: 'Quiz created successfully', quiz });
    } catch (error) {
        console.error("Error creating quiz:", error);
        res.status(500).json({ error: 'Failed to create quiz', message: error.message });
    }
};

exports.deleteQuiz = async (req, res) => {
    try {
        const { quizId } = req.params;
        const userId = req.user.id; 
        const userRole = req.user.role;

        const quiz = await Quiz.findByPk(quizId);

        if (!quiz) {
            return res.status(404).json({ message: 'Quiz not found' });
        }
        if (quiz.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized: Only the creator or an admin can delete this quiz' });
        }

        await quiz.destroy();

        return res.status(200).json({ message: 'Quiz deleted successfully' });
    } catch (error) {
        console.error("Error deleting quiz:", error);
        return res.status(500).json({ message: 'Server error deleting quiz', error: error.message });
    }
};

exports.updateQuiz = async (req, res) => {
    try {
        const { quizId } = req.params;
        const userId = req.user.id; 
        const userRole = req.user.role;
        const { title, timeLimit, negativeMarking, totalMarks, questions, courseId } = req.body;

        const quiz = await Quiz.findByPk(quizId); 

        if (!quiz) {
            return res.status(404).json({ error: 'Quiz not found' });
        }
        if (quiz.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ error: 'Unauthorized: You are not the creator or an admin of this quiz.' });
        }
        if (courseId && userRole === 'teacher') {
            const course = await db.Course.findByPk(courseId);
            const teacherProfile = await db.Teacher.findOne({ where: { userId: userId } });
            if (!course || !teacherProfile || course.teacherId !== teacherProfile.id) {
                return res.status(403).json({ message: 'You are not authorized to assign this quiz to the specified course.' });
            }
        }

        let questionIdCounter = 1;
        const questionsWithUniqueIds = questions ? questions.map(q => {
            return {
                ...q,
                id: q.id || questionIdCounter++ 
            };
        }) : quiz.questions; 

        await quiz.update({ 
            title: title !== undefined ? title : quiz.title,
            timeLimit: timeLimit !== undefined ? timeLimit : quiz.timeLimit,
            negativeMarking: negativeMarking !== undefined ? negativeMarking : quiz.negativeMarking,
            totalMarks: totalMarks !== undefined ? totalMarks : quiz.totalMarks,
            questions: questionsWithUniqueIds,
            courseId: courseId !== undefined ? courseId : quiz.courseId 
        });

        res.status(200).json({ message: 'Quiz updated successfully', quiz });

    } catch (error) {
        console.error("Error updating quiz:", error);
        res.status(500).json({ error: 'Failed to update quiz', message: error.message });
    }
};

exports.getQuizzes = async (req, res) => {
    try {
        const userRole = req.user.role; 

        let quizzes;
        if (userRole === 'admin' || userRole === 'teacher') {
            quizzes = await Quiz.findAll({ 
                include: [
                    {
                        model: User, 
                        as: 'creator', 
                        attributes: ['id', 'username', 'firstName', 'lastName']
                    },
                    {
                        model: db.Course, 
                        as: 'course', 
                        attributes: ['id', 'title']
                    }
                ]
            });
        } else {
             return res.status(403).json({ message: 'Unauthorized: Only teachers and admins can view all quizzes.' });
        }

        res.status(200).json(quizzes);
    } catch (err) {
        console.error("Error fetching quizzes:", err);
        res.status(500).json({ error: 'Failed to fetch quizzes', message: err.message });
    }
};

exports.getSubmissions = async (req, res) => {
    const { quizId } = req.params;
    const userId = req.user.id;
    const userRole = req.user.role;

    try {
        const quiz = await Quiz.findByPk(quizId);
        if (!quiz) {
            return res.status(404).json({ message: 'Quiz not found.' });
        }

        if (quiz.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized: You are not the creator of this quiz or an admin.' });
        }

        const submissions = await Submission.findAll({ 
            where: { quizId },
            include: [
                {
                    model: Student,
                    as: 'student',
                    include: [{ model: User, as: 'User', attributes: ['id', 'username', 'firstName', 'lastName'] }], 
                    attributes: ['id', 'enrollmentNumber']
                },
                {
                    model: Quiz,
                    as: 'quiz',
                    attributes: ['id', 'title', 'totalMarks'] 
                }
            ],
            order: [['submittedAt', 'DESC']]
        });
        res.status(200).json(submissions);
    } catch (err) {
        console.error("Error fetching submissions for quiz:", err);
        res.status(500).json({ error: 'Failed to fetch submissions', message: err.message });
    }
};

exports.getAllStudentSubmissions = async (req, res) => {
    const { studentId } = req.params;
    const userId = req.user.id;
    const userRole = req.user.role;

    console.log(`Fetching all submissions for Student ID: ${studentId}`);

    try {
        const studentProfile = await Student.findByPk(studentId); 
        if (!studentProfile) {
            return res.status(404).json({ message: 'Student profile not found.' });
        }
        if (userRole === 'student' && studentProfile.userId !== userId) {
            return res.status(403).json({ message: 'Unauthorized: You can only view your own submissions.' });
        }

        const submissions = await Submission.findAll({
            where: { studentId: studentId },
            include: [
                {
                    model: Quiz, 
                    as: 'quiz',
                    attributes: ['id', 'title', 'totalMarks']
                }
            ],
            order: [['submittedAt', 'DESC']]
        });

        const submissionSummaries = submissions.map(submission => {
            const quizTitle = submission.quiz ? submission.quiz.title : 'Unknown Quiz';
            const totalMarks = submission.quiz ? submission.quiz.totalMarks : 0;
            const percentage = totalMarks > 0 ? (submission.score / totalMarks) * 100 : 0.0;

            return {
                submissionId: submission.id,
                quizId: submission.quizId,
                quizTitle: quizTitle,
                score: submission.score,
                totalMarks: totalMarks,
                percentage: parseFloat(percentage.toFixed(2)),
                submittedAt: submission.submittedAt.toISOString()
            };
        });

        res.status(200).json(submissionSummaries);

    } catch (err) {
        console.error("Error fetching all student submissions:", err);
        res.status(500).json({ error: 'Failed to fetch all student reports', message: err.message });
    }
};

exports.submitQuiz = async (req, res) => {
    const { studentId, quizId, answers } = req.body;
    const userId = req.user.id; 

    console.log("Received quiz submission request:");
    console.log("studentId:", studentId);
    console.log("quizId:", quizId);
    console.log("answers:", answers);

    try {
        const studentProfile = await Student.findByPk(studentId); 
        if (!studentProfile) {
            return res.status(404).json({ message: 'Student profile not found for the given ID.' });
        }
        if (studentProfile.userId !== userId && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized: You can only submit quizzes for your own student profile.' });
        }

        const quiz = await Quiz.findByPk(quizId); 
        if (!quiz) {
            console.error("Quiz not found for ID:", quizId);
            return res.status(404).json({ error: 'Quiz not found' });
        }

        console.log("Fetched quiz questions structure:", quiz.questions);

        let score = 0;

        answers.forEach(studentAnswer => {
            const questionInQuiz = quiz.questions.find(q => q.id === studentAnswer.questionId);

            if (questionInQuiz) {
                const correctOptionString = questionInQuiz.options[questionInQuiz.correctOptionIndex];

                console.log(`Question ID: ${questionInQuiz.id}`);
                console.log(`Student Selected Option: "${studentAnswer.selectedOption}"`);
                console.log(`Correct Option String: "${correctOptionString}"`);
                console.log(`Is Correct: ${studentAnswer.selectedOption === correctOptionString}`);

                if (studentAnswer.selectedOption === correctOptionString) {
                    score += questionInQuiz.mark || 0;
                    console.log(`Correct! Current score: ${score}`);
                } else {
                    if (quiz.negativeMarking && quiz.negativeMarking > 0) {
                        score -= quiz.negativeMarking;
                        console.log(`Incorrect. Applied negative marking. Current score: ${score}`);
                    } else {
                        console.log(`Incorrect. No negative marking applied. Current score: ${score}`);
                    }
                }
            } else {
                console.warn(`Question ID ${studentAnswer.questionId} not found in quiz questions.`);
            }
        });

        console.log("Final calculated score:", score);

        const submission = await Submission.create({ 
            studentId,
            quizId,
            answers: answers,
            score,
            submittedAt: new Date() 
        });

        console.log("Submission created successfully:", submission.toJSON());

        res.status(201).json(submission);
    } catch (err) {
        console.error("Error submitting quiz:", err);
        res.status(500).json({ error: 'Failed to submit quiz', message: err.message });
    }
}

exports.getStudentReport = async (req, res) => {
    const { quizId, studentId } = req.params;
    const userId = req.user.id; 
    const userRole = req.user.role;

    console.log(`Fetching report for Quiz ID: ${quizId} and Student ID: ${studentId}`);

    try {
        const studentProfile = await Student.findByPk(studentId); 
        if (!studentProfile) {
            return res.status(404).json({ message: 'Student profile not found.' });
        }
        if (userRole === 'student' && studentProfile.userId !== userId) {
            return res.status(403).json({ message: 'Unauthorized: You can only view your own reports.' });
        }

        const submission = await Submission.findOne({ 
            where: {
                quizId: quizId,
                studentId: studentId
            },
            include: [
                {
                    model: Quiz, 
                    as: 'quiz',
                    attributes: ['id', 'title', 'totalMarks', 'negativeMarking', 'timeLimit', 'questions']
                },
                {
                    model: Student, 
                    as: 'student',
                    attributes: ['id', 'userId'],
                    include: [
                        {
                            model: User, 
                            as: 'User', 
                            attributes: ['username', 'firstName', 'lastName'] 
                        }
                    ]
                }
            ]
        });

        if (!submission) {
            console.log(`No submission found for Quiz ID: ${quizId} and Student ID: ${studentId}`);
            return res.status(404).json({ message: 'Quiz report not found for this quiz and student.' });
        }

        const questionsForReport = submission.quiz.questions.map(q => {
            const studentAnswer = submission.answers.find(ans => ans.questionId === q.id);
            const isCorrect = studentAnswer && (studentAnswer.selectedOption === q.options[q.correctOptionIndex]);

            return {
                questionId: q.id,
                questionText: q.questionText,
                options: q.options,
                correctOptionIndex: q.correctOptionIndex,
                correctAnswer: q.options[q.correctOptionIndex],
                studentSelectedOption: studentAnswer ? studentAnswer.selectedOption : null,
                isCorrect: studentAnswer ? isCorrect : false,
                marks: q.mark || 0
            };
        });

        const reportData = {
            submissionId: submission.id,
            quizId: submission.quizId,
            quizTitle: submission.quiz ? submission.quiz.title : 'Unknown Quiz',
            studentId: submission.studentId,
            studentUsername: submission.student && submission.student.User
                                    ? submission.student.User.username
                                    : 'Unknown Student',
            studentFirstName: submission.student && submission.student.User
                                    ? submission.student.User.firstName
                                    : 'Unknown',
            studentLastName: submission.student && submission.student.User
                                    ? submission.student.User.lastName
                                    : 'Unknown',
            score: submission.score,
            totalMarks: submission.quiz ? submission.quiz.totalMarks : 0,
            percentage: submission.quiz && submission.quiz.totalMarks > 0
                                    ? parseFloat(((submission.score / submission.quiz.totalMarks) * 100).toFixed(2))
                                    : 0.0,
            questions: questionsForReport,
            submittedAt: submission.submittedAt ? submission.submittedAt.toISOString() : null 
        };

        res.status(200).json(reportData);

    } catch (err) {
        console.error("Error fetching student report:", err);
        res.status(500).json({ error: 'Failed to fetch student report', message: err.message });
    }
};

exports.getQuizzesForStudents = async (req, res) => {
    try {
        const userId = req.user.id; 
        const student = await db.Student.findOne({ 
            where: { userId: userId } 
        });

        if (!student) {
            return res.status(404).json({ message: 'Student profile not found.' });
        }
        const enrolledCourses = await student.getCourses({
            attributes: ['id']
        });
        const courseIds = enrolledCourses.map(c => c.id);

        if (courseIds.length === 0) {
            return res.status(200).json([]); 
        }

        console.log(`Student enrolled in Course IDs: ${courseIds}`);
        const quizzes = await db.Quiz.findAll({
            where: {
                courseId: {
                    [Op.in]: courseIds 
                }
            },
            include: [
                {
                    model: db.Course,
                    as: 'course',
                    attributes: ['id', 'title']
                },
                {
                    model: db.User, 
                    as: 'creator',
                    attributes: ['firstName', 'lastName']
                }
            ],
            order: [['createdAt', 'DESC']]
        });

        res.status(200).json(quizzes);

    } catch (err) {
        console.error("Error fetching student quizzes:", err);
        res.status(500).json({ error: 'Failed to fetch quizzes', message: err.message });
    }
};

exports.getQuizById = async (req, res) => {
    try {
        const { quizId } = req.params;

        const quiz = await Quiz.findByPk(quizId, { 
            include: [
                {
                    model: db.User, 
                    as: 'creator',
                    attributes: ['id', 'username', 'firstName', 'lastName']
                },
                {
                    model: db.Course, 
                    as: 'course',
                    attributes: ['id', 'title']
                }
            ]
        });

        if (!quiz) {
            return res.status(404).json({ message: 'Quiz not found' });
        }

        res.status(200).json(quiz);
    } catch (error) {
        console.error("Error fetching quiz by ID:", error);
        res.status(500).json({
            error: 'Failed to fetch quiz',
            message: error.message
        });
    }
};

module.exports = {
    createQuiz: exports.createQuiz,
    deleteQuiz: exports.deleteQuiz,
    updateQuiz: exports.updateQuiz,
    getQuizzes: exports.getQuizzes,
    getSubmissions: exports.getSubmissions,
    getAllStudentSubmissions: exports.getAllStudentSubmissions,
    submitQuiz: exports.submitQuiz,
    getStudentReport: exports.getStudentReport,
    getQuizzesForStudents: exports.getQuizzesForStudents,
    getQuizById: exports.getQuizById
};