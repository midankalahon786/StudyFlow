const express = require('express');
const router = express.Router();
const upload = require('../multerConfig');
const Course = require('../models/course');
const Student = require('../models/student');
const authenticateUser = require('../middleware/authMiddleware');
const { Op } = require('sequelize');
const { User } = require('../models/associations');


// Route for creating a new course with optional file and assigned users
router.post('/create-with-extras', authenticateUser, upload.single('file'), async (req, res) => {
    try {
        const { title, description } = req.body;
        const file = req.file;

        // Parse assignedUsers from JSON string (sent as multipart field)
        let usersArray = [];
        if (req.body.assignedUsers) {
            try {
                usersArray = JSON.parse(req.body.assignedUsers);
                if (!Array.isArray(usersArray)) {
                    return res.status(400).json({ message: 'assignedUsers must be a JSON array.' });
                }
            } catch (parseError) {
                return res.status(400).json({ message: 'Invalid JSON format in assignedUsers.' });
            }
        }

        // Validate required fields
        if (!title || !description || usersArray.length === 0) {
            return res.status(400).json({ message: 'Title, description, and at least one assigned user are required.' });
        }

        // Prepare course data
        const courseData = {
            title,
            description,
            assignedUsers: usersArray,
            createdBy: req.user.id, // Store as INTEGER (user ID)
        };

        if (file) {
            courseData.fileUrl = `/uploads/${file.filename}`;
        }

        const newCourse = await Course.create(courseData);

        res.status(201).json({
            message: 'Course created successfully',
            course: newCourse,
        });
    } catch (error) {
        console.error('Error creating course:', error);
        res.status(500).json({ message: 'Failed to create course', error: error.message });
    }
});

// Route to list courses based on user role
router.get('/list', authenticateUser, async (req, res) => {
    try {
        const user = req.user;
        let courses;

        if (user.role === 'teacher') {
            // Teachers see courses they created
            courses = await Course.findAll({
                where: { createdBy: user.id }
            });
        } else {
            // Students see only courses assigned to them
             courses = await Course.findAll({
                where: {
                    assignedUsers: {
                        [Op.contains]: [user.id.toString()] // Ensure type match
                    }
                }
            });
        }
        console.log('Courses fetched:', courses);
        // Log the fetched courses for debugging
        res.status(200).json({ courses });
    } catch (error) {
        console.error('Error fetching courses:', error);
        res.status(500).json({ message: 'Failed to fetch courses', error: error.message });
    }
});

// Route for a student to get their assigned courses
router.get('/my-courses', authenticateUser, async (req, res) => {
    try {
        const user = req.user;

        if (user.role !== 'student') {
            return res.status(403).json({ message: 'You do not have permission to access this route.' });
        }
      const student = await Student.findOne({ where: { userId: user.id } });
        if(!student){
             return res.status(404).json({ message: 'Student not found.' });
        }
        // Students see only courses assigned to them
        const courses = await Course.findAll({
            where: {
                assignedUsers: {
                  [Op.contains]: [student.enrollmentNumber]
                }
            }
        });

        res.status(200).json({ courses });
    } catch (error) {
        console.error('Error fetching student courses:', error);
        res.status(500).json({ message: 'Failed to fetch courses', error: error.message });
    }
});


router.get('/students', authenticateUser, async (req, res) => {
    try {
        const user = req.user;

        if (user.role !== 'admin' && user.role !== 'teacher') {
            return res.status(403).json({ message: 'You do not have permission to view students.' });
        }

        // Fetch students with the associated User model (including firstName and lastName)
        const students = await Student.findAll({
            include: {
                model: User,
                attributes: ['firstName', 'lastName'] // Include firstName and lastName fields
            }
        });

        if (!students || students.length === 0) {
            return res.status(404).json({ message: 'No students found.' });
        }

        // Format the student data to include firstName and lastName
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

// Route to get students assigned to a specific course
router.get('/students/:courseId', authenticateUser, async (req, res) => {
    try {
        const { courseId } = req.params;

        // Find the course
        const course = await Course.findByPk(courseId);

        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        const assignedEnrollmentNumbers = course.assignedUsers;

        const students = await Student.findAll({
            where: {
                enrollmentNumber: {
                    [Op.in]: assignedEnrollmentNumbers
                }
            },
            include: {
                model: User,
                attributes: ['firstName', 'lastName']
            }
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

// Route to update the students assigned to a course
router.put('/:courseId/students', authenticateUser, async (req, res) => {
    try {
        const { courseId } = req.params;
        const { studentIds } = req.body;

        const course = await Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        if (!Array.isArray(studentIds)) {
            return res.status(400).json({ message: 'studentIds must be an array' });
        }

        // Use Sequelize's setStudents method to update the association
        await course.setStudents(studentIds);

        // Fetch the updated course data with associated students
        const updatedCourse = await Course.findByPk(courseId, {
            include: [{
                model: Student,
                as: 'students', // Use the alias defined in the association
                include: {
                    model: User,
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
            lastName: updatedCourse.User?.lastName || 'Unnamed'
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

// Route to delete a course by ID
router.delete('/delete/:id', authenticateUser, async (req, res) => {
    try {
        const { id } = req.params;
        const user = req.user;

        const course = await Course.findOne({ where: { id } });

        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        if (course.createdBy !== user.id && user.role !== 'admin') {
            return res.status(403).json({ message: 'You do not have permission to delete this course' });
        }

        await Course.destroy({ where: { id } });

        res.status(200).json({ message: 'Course deleted successfully' });
    } catch (error) {
        console.error('Error deleting course:', error);
        res.status(500).json({ message: 'Failed to delete course', error: error.message });
    }
});

// Route to delete a student from a course
router.delete('/:courseId/students/:studentId', authenticateUser, async (req, res) => {
    try {
        const { courseId, studentId } = req.params;
        const user = req.user;

        // Find the course
        const course = await Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found' });
        }

        // Find the student
        const student = await Student.findByPk(studentId);
        if (!student) {
            return res.status(404).json({ message: 'Student not found' });
        }

        // Check if the student is assigned to the course
        const isAssigned = course.assignedUsers.includes(student.enrollmentNumber);
        if (!isAssigned) {
            return res.status(400).json({ message: 'Student is not assigned to this course' });
        }
        // Remove the student's enrollmentNumber from the course's assignedUsers array
        const updatedAssignedUsers = course.assignedUsers.filter(enr => enr !== student.enrollmentNumber);
        course.assignedUsers = updatedAssignedUsers; // important
        await course.save();

        res.status(200).json({ message: 'Student removed from course successfully' });
    } catch (error) {
        console.error('Error deleting student from course:', error);
        res.status(500).json({ message: 'Failed to remove student from course', error: error.message });
    }
});

// Middleware for logging incoming requests
router.use((req, res, next) => {
    console.log('Incoming request:', req.method, req.originalUrl);
    next();
});

module.exports = router;
