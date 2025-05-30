const Quiz = require('../models/quiz');
const Submission = require('../models/submissions');
const Student = require('../models/student');
const User = require('../models/User');
const { Op } = require('sequelize');

// Create a new quiz
exports.createQuiz = async (req, res) => {
    try {
        const { title, timeLimit, negativeMarking, totalMarks, questions } = req.body;

        console.log("Create Quiz Request Body:", req.body); // Log the entire body
        console.log("Title present:", !!title);
        console.log("timeLimit present:", !!timeLimit);
        console.log("negativeMarking present:", !!negativeMarking);
        console.log("totalMarks present:", !!totalMarks);
        console.log("questions present:", !!questions);

        if (typeof title === 'undefined' || typeof timeLimit === 'undefined' || typeof negativeMarking === 'undefined' || typeof totalMarks === 'undefined' || typeof questions === 'undefined') {
            return res.status(400).json({
                error: 'All fields are required'
            });
        }


        let questionIdCounter = 1; 
        const questionsWithUniqueIds = questions.map(q => {
            // Ensure the question object is spread to keep existing properties
            return {
                ...q,
                id: questionIdCounter++ 
            };
        });
        // --- IMPORTANT CHANGE ENDS HERE ---

        const quiz = await Quiz.create({
            title,
            timeLimit,
            negativeMarking,
            totalMarks,
            questions: questionsWithUniqueIds, // Use the modified array with unique IDs
            createdBy: req.userId, // Assign the creator of the quiz
        });

        res.status(201).json({
            message: 'Quiz created successfully',
            quiz
        });
    } catch (error) {
        console.error("Error creating quiz:", error);
        res.status(500).json({
            error: 'Failed to create quiz',
            message: error.message
        });
    }
};


exports.deleteQuiz = async (req, res) => {
    try {
        const { quizId } = req.params;
        const userId = req.userId; // Use req.userId from authMiddleware

        const quiz = await Quiz.findByPk(quizId);

        if (!quiz) {
            return res.status(404).json({ message: 'Quiz not found' });
        }

        // Ensure only the creator can delete
        if (quiz.createdBy !== userId) {
            return res.status(403).json({ message: 'Unauthorized: Only the creator can delete this quiz' });
        }

        await quiz.destroy();

        return res.status(200).json({ message: 'Quiz deleted successfully' });
    } catch (error) {
        console.error("Error deleting quiz:", error); // Added detailed logging
        return res.status(500).json({ message: 'Server error deleting quiz', error: error.message });
    }
};

exports.updateQuiz = async (req, res) => {
    try {
        const { quizId } = req.params;
        const userId = req.userId; // Assuming req.userId is set by authMiddleware
        const { title, timeLimit, negativeMarking, totalMarks, questions } = req.body;

        const quiz = await Quiz.findByPk(quizId);

        if (!quiz) {
            return res.status(404).json({ error: 'Quiz not found' });
        }

        // Authorization: Only the creator can update the quiz
        if (quiz.createdBy !== userId) {
            return res.status(403).json({ error: 'Unauthorized: You are not the creator of this quiz.' });
        }

        // Optional: Re-assign unique IDs to questions if they are being updated
        // This is important if questions can be added/removed during an edit.
        let questionIdCounter = 1; // Or use uuidv4()
        const questionsWithUniqueIds = questions.map(q => {
            return {
                ...q,
                id: q.id || questionIdCounter++ // Keep existing ID if present, otherwise assign new
            };
        });

        // Update the quiz fields
        await quiz.update({
            title: title !== undefined ? title : quiz.title,
            timeLimit: timeLimit !== undefined ? timeLimit : quiz.timeLimit,
            negativeMarking: negativeMarking !== undefined ? negativeMarking : quiz.negativeMarking,
            totalMarks: totalMarks !== undefined ? totalMarks : quiz.totalMarks,
            questions: questionsWithUniqueIds // Update with potentially new/modified questions
        });

        res.status(200).json({ message: 'Quiz updated successfully', quiz });

    } catch (error) {
        console.error("Error updating quiz:", error);
        res.status(500).json({ error: 'Failed to update quiz', message: error.message });
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

exports.getAllStudentSubmissions = async (req, res) => {
    const { studentId } = req.params;
    console.log(`Workspaceing all submissions for Student ID: ${studentId}`);

    try {
        const submissions = await Submission.findAll({
            where: { studentId: studentId },
            include: [
                {
                    model: Quiz,
                    as: 'quiz',
                    attributes: ['id', 'title', 'totalMarks'] // Only need summary info from Quiz
                }
            ],
            order: [['submittedAt', 'DESC']] // Order by most recent submission
        });

        // Map submissions to a summary format for the frontend
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
                percentage: parseFloat(percentage.toFixed(2)), // Format to 2 decimal places
                submittedAt: submission.submittedAt.toISOString() // Send as ISO string
            };
        });

        res.status(200).json(submissionSummaries);

    } catch (err) {
        console.error("Error fetching all student submissions:", err);
        res.status(500).json({ error: 'Failed to fetch all student reports', message: err.message });
    }
};

exports.submitQuiz = async (req, res) => {
    const { studentId, quizId, answers } = req.body; // 'answers' is expected to be an array of { questionId: Int, selectedOption: String }

    // Log the incoming request body for debugging
    console.log("Received quiz submission request:");
    console.log("studentId:", studentId);
    console.log("quizId:", quizId);
    console.log("answers:", answers); // Crucial to inspect this structure

    try {
        const quiz = await Quiz.findByPk(quizId);
        if (!quiz) {
            console.error("Quiz not found for ID:", quizId);
            return res.status(404).json({ error: 'Quiz not found' });
        }

        // Log the fetched quiz data to inspect its question structure
        console.log("Fetched quiz questions structure:", quiz.questions);

        let score = 0; // Initialize score

        // Iterate through the student's submitted answers
        answers.forEach(studentAnswer => {
            // Find the corresponding question in the quiz's actual questions
            const questionInQuiz = quiz.questions.find(q => q.id === studentAnswer.questionId);

            if (questionInQuiz) {
                const correctOptionString = questionInQuiz.options[questionInQuiz.correctOptionIndex];

                // Log for debugging comparison
                console.log(`Question ID: ${questionInQuiz.id}`);
                console.log(`Student Selected Option: "${studentAnswer.selectedOption}"`);
                console.log(`Correct Option String: "${correctOptionString}"`);
                console.log(`Is Correct: ${studentAnswer.selectedOption === correctOptionString}`);

                if (studentAnswer.selectedOption === correctOptionString) {
                    score += questionInQuiz.mark || 0; // Add marks, default to 0 if mark is undefined
                    console.log(`Correct! Current score: ${score}`);
                } else {
                    // Apply negative marking if enabled and question has a mark
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

        console.log("Final calculated score:", score); // Log the final calculated score

        // Save submission
        const submission = await Submission.create({
            studentId,
            quizId,
            answers: answers, // Store the student's submitted answers directly as JSONB
            score,             // This 'score' variable is the one that gets stored
        });

        console.log("Submission created successfully:", submission.toJSON()); // Log the created submission
    
        res.status(201).json(submission);
    } catch (err) {
        console.error("Error submitting quiz:", err); // Log the detailed error
        res.status(500).json({ error: 'Failed to submit quiz', message: err.message });
    }
}
exports.getStudentReport = async (req, res) => {
    const { quizId, studentId } = req.params;

    console.log(`Fetching report for Quiz ID: ${quizId} and Student ID: ${studentId}`);

    try {
        const submission = await Submission.findOne({
            where: {
                quizId: quizId,
                studentId: studentId
            },
            include: [
                {
                    model: Quiz,
                    as: 'quiz',
                    // Ensure 'title' is listed in attributes
                    attributes: ['id', 'title', 'totalMarks', 'negativeMarking', 'timeLimit', 'questions']
                },
                {
                    model: Student,
                    as: 'student',
                    attributes: ['id', 'userId'],
                    include: [
                        {
                            model: User,
                            // This alias MUST match the alias used in your Sequelize association (models/index.js)
                            // Based on previous errors, 'User' (capital U) is likely correct.
                            as: 'User', // <--- CORRECT ALIAS HERE (Capital 'U')
                            attributes: ['username']
                        }
                    ]
                }
            ]
        });

        if (!submission) {
            console.log(`No submission found for Quiz ID: ${quizId} and Student ID: ${studentId}`);
            return res.status(404).json({ message: 'Quiz report not found for this quiz and student.' });
        }

        // Prepare questions for the report
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

        // Construct the reportData object
        const reportData = {
            submissionId: submission.id,
            quizId: submission.quizId,
            quizTitle: submission.quiz ? submission.quiz.title : 'Unknown Quiz',
            studentId: submission.studentId,
            studentUsername: submission.student && submission.student.User
                                ? submission.student.User.username 
                                : 'Unknown Student',
            score: submission.score,
            totalMarks: submission.quiz ? submission.quiz.totalMarks : 0,
            percentage: submission.quiz && submission.quiz.totalMarks > 0
                                ? parseFloat(((submission.score / submission.quiz.totalMarks) * 100).toFixed(2))
                                : 0.0,
            questions: questionsForReport
        };

        res.status(200).json(reportData);

    } catch (err) {
        console.error("Error fetching student report:", err);
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

// Get a quiz by its ID
exports.getQuizById = async (req, res) => {
    try {
        const { quizId } = req.params;

        const quiz = await Quiz.findByPk(quizId); 

        if (!quiz) {
            return res.status(404).json({ message: 'Quiz not found' });
        }

        res.status(200).json(quiz);
    } catch (error) {
        // Log the error for debugging purposes
        console.error("Error fetching quiz by ID:", error);
        res.status(500).json({
            error: 'Failed to fetch quiz',
            message: error.message
        });
    }
};