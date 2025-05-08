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

// Middleware for logging incoming requests
router.use((req, res, next) => {
  console.log('Incoming request:', req.method, req.originalUrl);
  next();
});

module.exports = router;
