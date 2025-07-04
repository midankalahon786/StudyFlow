const express = require('express');
const router = express.Router();

const upload = require('../multerConfig');

const db = require('../models');
const { Op } = require('sequelize');
const fs = require('fs');
const path = require('path');
const authenticateUser = require('../middleware/authMiddleware');
const authorizeRole = require('../middleware/authorizeRole');
const courseController = require('../controllers/courseController'); // Import the course controller

router.use((req, res, next) => {
    console.log(`[COURSE_ROUTE] Incoming request: ${req.method} ${req.originalUrl}. User: ${req.user ? req.user.username : 'N/A'}`);
    next();
});

// Route to create a new course with file upload and student assignment
router.post('/create-with-extras', authenticateUser, upload.single('file'), async (req, res) => {
    console.log('Received request for /create-with-extras');
    console.log('Request Body:', req.body);
    console.log('Request File:', req.file);

    try {
        const { title, description } = req.body;
        const file = req.file;
        const userId = req.user.id; // User ID from authenticated token
        const userRole = req.user.role;

        // Authorization check: Only teachers or admins can create courses
        if (userRole !== 'teacher' && userRole !== 'admin') {
            return res.status(403).json({ message: 'Only teachers or administrators can create courses.' });
        }

        let teacherId = null;
        if (userRole === 'teacher') {
            const teacher = await db.Teacher.findOne({ where: { userId: userId } });
            if (!teacher) {
                return res.status(403).json({ message: 'Teacher profile not found for this user.' });
            }
            teacherId = teacher.id; // This is the ID from the Teacher model
        }

        let studentIds = [];
        if (req.body.assignedUsers) {
            try {
                studentIds = JSON.parse(req.body.assignedUsers);
                if (!Array.isArray(studentIds) || !studentIds.every(id => typeof id === 'string' || typeof id === 'number')) {
                    return res.status(400).json({ message: 'assignedUsers must be a JSON array of student IDs.' });
                }
            } catch (parseError) {
                console.error("Failed to parse assignedUsers:", parseError);
                return res.status(400).json({ message: 'Invalid JSON format in assignedUsers.' });
            }
        }
        console.log('Parsed Student IDs:', studentIds);

        // Validate required fields
        if (!title || !description || studentIds.length === 0) {
            // If a file was uploaded but validation fails, clean it up
            if (file) {
                const filePath = path.join(__dirname, '..', 'uploads', 'course_resources', file.filename);
                fs.unlink(filePath, (err) => {
                    if (err) console.error('Error deleting uploaded file:', err);
                });
            }
            return res.status(400).json({ message: 'Title, description, and at least one assigned student are required.' });
        }

        // Prepare course data
        const courseData = {
            title,
            description,
            createdBy: userId,
            teacherId: teacherId,
        };

        if (file) {
            // CRITICAL FIX 3: Correct the fileUrl path to match multerConfig's destination
            courseData.fileUrl = `/uploads/course_resources/${file.filename}`;
        }

        const newCourse = await db.Course.create(courseData);

        // Assign students using the Many-to-Many association
        if (studentIds.length > 0) {
            await newCourse.setStudents(studentIds);
        }

        const courseWithStudents = await db.Course.findByPk(newCourse.id, {
            include: [{
                model: db.Student,
                as: 'students',
                include: [{
                    model: db.User,
                    attributes: ['firstName', 'lastName']
                }],
                attributes: ['id', 'enrollmentNumber', 'department', 'semester', 'batchYear', 'userId']
            }]
        });

        const formattedStudents = courseWithStudents.students.map(student => ({
            id: student.id,
            enrollmentNumber: student.enrollmentNumber,
            department: student.department,
            semester: student.semester,
            batchYear: student.batchYear,
            userId: student.userId,
            firstName: student.User?.firstName || 'Unnamed',
            lastName: student.User?.lastName || 'Unnamed'
        }));


        res.status(201).json({
            message: 'Course created successfully',
            course: {
                ...courseWithStudents.toJSON(),
                students: formattedStudents
            },
        });
    } catch (error) {
        console.error('Error creating course:', error);
        // If an error occurs after file upload but before DB operation, delete the file
        if (req.file) {
            const filePath = path.join(__dirname, '..', 'uploads', 'course_resources', req.file.filename);
            fs.unlink(filePath, (err) => {
                if (err) console.error('Error deleting uploaded file on error:', err);
            });
        }
        res.status(500).json({ message: 'Failed to create course', error: error.message });
    }
});

// Route to list courses based on user role
router.get('/list', authenticateUser, async (req, res) => {
    try {
        const user = req.user;
        let courses;

        if (user.role === 'teacher') {
            const teacher = await db.Teacher.findOne({ where: { userId: user.id } });
            if (!teacher) {
                return res.status(403).json({ message: 'Teacher profile not found.' });
            }
            courses = await db.Course.findAll({
                where: { teacherId: teacher.id },
                include: [{
                    model: db.Student,
                    as: 'students',
                    include: [{ model: db.User, attributes: ['firstName', 'lastName'] }]
                }]
            });
        } else if (user.role === 'student') {
            const student = await db.Student.findOne({ where: { userId: user.id } });
            if (!student) {
                return res.status(403).json({ message: 'Student profile not found.' });
            }
            courses = await student.getCourses({
                include: [{
                    model: db.User,
                    as: 'creator',
                    attributes: ['firstName', 'lastName']
                }, {
                    model: db.Teacher,
                    as: 'teacher',
                    include: [{ model: db.User, attributes: ['firstName', 'lastName'] }],
                    attributes: ['id', 'department', 'designation']
                }]
            });
        } else if (user.role === 'admin') {
            courses = await db.Course.findAll({
                include: [{
                    model: db.Student,
                    as: 'students',
                    include: [{ model: db.User, attributes: ['firstName', 'lastName'] }]
                }, {
                    model: db.User,
                    as: 'creator',
                    attributes: ['firstName', 'lastName']
                }, {
                    model: db.Teacher,
                    as: 'teacher',
                    include: [{ model: db.User, attributes: ['firstName', 'lastName'] }],
                    attributes: ['id', 'department', 'designation']
                }]
            });
        } else {
            return res.status(403).json({ message: 'Your role does not have permission to view courses.' });
        }
        res.status(200).json({ courses });
    } catch (error) {
        console.error('Error fetching courses:', error);
        res.status(500).json({ message: 'Failed to fetch courses', error: error.message });
    }
});

// Route for a student to get their assigned courses (redundant with /list for students, can remove or keep for specific use)
router.get('/my-courses', authenticateUser, authorizeRole(['student']), async (req, res) => {
    try {
        const user = req.user;
        const student = await db.Student.findOne({ where: { userId: user.id } });
        if (!student) {
            return res.status(404).json({ message: 'Student profile not found.' });
        }
        const courses = await student.getCourses({
            include: [{
                model: db.User,
                as: 'creator',
                attributes: ['firstName', 'lastName']
            }, {
                model: db.Teacher,
                as: 'teacher',
                include: [{ model: db.User, attributes: ['firstName', 'lastName'] }],
                attributes: ['id', 'department', 'designation']
            }]
        });

        res.status(200).json({ courses });
    } catch (error) {
        console.error('Error fetching student courses:', error);
        res.status(500).json({ message: 'Failed to fetch courses', error: error.message });
    }
});

// Route to get all students (for admin/teacher to assign)
router.get('/students', authenticateUser, authorizeRole(['admin', 'teacher']), async (req, res) => {
    try {
        const students = await db.Student.findAll({
            include: [
                {
                    model: db.User,
                    attributes: ['firstName', 'lastName'],
                    as: 'User'
                },
                {
                    model: db.Course,
                    as: 'courses',
                    through: { attributes: [] },
                    required: false
                }
            ],

        });

        if (!students || students.length === 0) {
            return res.status(200).json({ students: [] });
        }

        const formattedStudents = students.map(student => ({
            id: student.id,
            enrollmentNumber: student.enrollmentNumber,
            department: student.department,
            semester: student.semester,
            batchYear: student.batchYear,
            userId: student.userId,
            firstName: student.User?.firstName || 'Unnamed',
            lastName: student.User?.lastName || 'Unnamed'
        }));

        res.status(200).json({ students: formattedStudents });
    } catch (error) {
        console.error('Error fetching students:', error);
        res.status(500).json({ message: 'Failed to fetch students', error: error.message });
    }
});

// Route to get students enrolled in a specific course
router.get('/:courseId/students', authenticateUser, async (req, res) => {
    try {
        const { courseId } = req.params;
        const user = req.user;

        const course = await db.Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        const teacher = await db.Teacher.findOne({ where: { userId: user.id } });
        const isTeacher = user.role === 'teacher' && teacher && course.teacherId === teacher.id;
        const isAdmin = user.role === 'admin';
        const currentStudentProfile = await db.Student.findOne({ where: { userId: user.id } });
        const isEnrolledStudent = user.role === 'student' && currentStudentProfile && (await course.hasStudent(currentStudentProfile));


        if (!isTeacher && !isAdmin && !isEnrolledStudent) {
            return res.status(403).json({ message: 'You do not have permission to view students for this course.' });
        }

        const students = await course.getStudents({
            include: [{
                model: db.User,
                attributes: ['firstName', 'lastName']
            }],
            attributes: ['id', 'enrollmentNumber', 'department', 'semester', 'batchYear', 'userId']
        });

        const formattedStudents = students.map(student => ({
            id: student.id,
            enrollmentNumber: student.enrollmentNumber,
            department: student.department,
            semester: student.semester,
            batchYear: student.batchYear,
            userId: student.userId,
            firstName: student.User?.firstName || 'Unnamed',
            lastName: student.User?.lastName || 'Unnamed'
        }));

        res.status(200).json({ students: formattedStudents });
    } catch (error) {
        console.error('Error fetching students by course:', error);
        res.status(500).json({ message: 'Failed to fetch students', error: error.message });
    }
});

// Route to update (assign/unassign) students for a course (Teacher/Admin only)
router.put('/:courseId/students', authenticateUser, async (req, res) => {
    try {
        const { courseId } = req.params;
        const { studentIds } = req.body;
        const user = req.user;

        const course = await db.Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        const teacher = await db.Teacher.findOne({ where: { userId: user.id } });

        if (!teacher || (course.teacherId !== teacher.id && user.role !== 'admin')) {
            return res.status(403).json({ message: 'You do not have permission to update students for this course.' });
        }

        if (!Array.isArray(studentIds)) {
            return res.status(400).json({ message: 'studentIds must be an array of student IDs.' });
        }

        console.log('Attempting to assign students with IDs:', studentIds);

        await course.setStudents(studentIds);

        const updatedCourse = await db.Course.findByPk(courseId, {
            include: [{
                model: db.Student,
                as: 'students',
                include: {
                    model: db.User,
                    attributes: ['firstName', 'lastName']
                },
                attributes: ['id', 'enrollmentNumber', 'department', 'semester', 'batchYear', 'userId']
            }]
        });
        const formattedStudents = updatedCourse.students.map(student => ({
            id: student.id,
            enrollmentNumber: student.enrollmentNumber,
            department: student.department,
            semester: student.semester,
            batchYear: student.batchYear,
            userId: student.userId,
            firstName: student.User?.firstName || 'Unnamed',
            lastName: student.User?.lastName || 'Unnamed'
        }));
        res.status(200).json({
            message: 'Course students updated successfully',
            course: {
                ...updatedCourse.toJSON(),
                students: formattedStudents
            }
        });

    } catch (error) {
        console.error('Error updating course students:', error);
        res.status(500).json({ message: 'Failed to update course students', error: error.message });
    }
});

// Route to delete a course
router.delete('/delete/:id', authenticateUser, async (req, res) => {
    try {
        const { id } = req.params;
        const user = req.user;

        const course = await db.Course.findByPk(id);

        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        const teacher = await db.Teacher.findOne({ where: { userId: user.id } });

        if (!teacher || (course.teacherId !== teacher.id && user.role !== 'admin')) {
            return res.status(403).json({ message: 'You do not have permission to delete this course.' });
        }

        // Before deleting the course, if it has a fileUrl, delete the associated file
        if (course.fileUrl) {
            const filePath = path.join(__dirname, '..', course.fileUrl); // e.g., 'backend/uploads/course_resources/filename.ext'
            fs.unlink(filePath, (err) => {
                if (err) {
                    console.error(`Error deleting course file ${filePath}:`, err);
                    // Decide if you want to block deletion or just log the error
                } else {
                    console.log(`Successfully deleted course file: ${filePath}`);
                }
            });
        }

        await db.Course.destroy({ where: { id } });

        res.status(200).json({ message: 'Course deleted successfully' });
    } catch (error) {
        console.error('Error deleting course:', error);
        res.status(500).json({ message: 'Failed to delete course', error: error.message });
    }
});

// Route to remove a single student from a course (Teacher/Admin only)
router.delete('/:courseId/students/:studentId', authenticateUser, async (req, res) => {
    try {
        const { courseId, studentId } = req.params;
        const user = req.user;

        const course = await db.Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        const student = await db.Student.findByPk(studentId);
        if (!student) {
            return res.status(404).json({ message: 'Student not found.' });
        }

        const teacher = await db.Teacher.findOne({ where: { userId: user.id } });

        if (!teacher || (course.teacherId !== teacher.id && user.role !== 'admin')) {
            return res.status(403).json({ message: 'You do not have permission to remove students from this course.' });
        }

        await course.removeStudent(student);

        res.status(200).json({ message: 'Student removed from course successfully' });
    } catch (error) {
        console.error('Error removing student from course:', error);
        res.status(500).json({ message: 'Failed to remove student from course', error: error.message });
    }
});
module.exports = router