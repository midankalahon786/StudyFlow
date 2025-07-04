const Course = require('../models/course');
const path = require('path');

// Get all courses
exports.getCourses = async (req, res) => {
    try {
        // Fetch all courses from the database
        const courses = await Course.findAll();
        res.json(courses); // Send courses as JSON
    } catch (err) {
        res.status(500).json({ error: err.message }); // Handle errors
    }
};

// Create new course
exports.createCourse = async (req, res) => {
    const { title, description, assignedUsers } = req.body;
    const file = req.file;

    // Validation for required fields
    if (!title || !description || !assignedUsers) {
        return res.status(400).json({ error: 'Title, description, and assigned users are required.' });
    }

    try {
        // Handle file upload URL (if file is uploaded)
        let fileUrl = null;
        if (file) {
            // Save the file URL to the course entry
            fileUrl = `/uploads/${file.filename}`;
        }

        // Save course to the database
        const course = await Course.create({
            title,
            description,
            assignedUsers: assignedUsers.split(',').map(user => user.trim()), // Convert comma-separated users into an array
            fileUrl,
            createdBy: req.user.id  // Assuming authentication middleware sets req.user.id
        });

        res.status(201).json(course); // Respond with the created course data
    } catch (err) {
        res.status(400).json({ error: err.message }); // Handle validation errors
    }
};

exports.uploadCourseResource = async (req, res) => {
    try {
      const { courseId } = req.params;
      const { title, description } = req.body;
      const userId = req.user.id; // User ID from auth token
  
      if (!req.file) {
        return res.status(400).json({ message: 'No file uploaded.' });
      }
  
      // Get the teacher's ID from their userId
      const teacher = await Teacher.findOne({ where: { userId: userId } });
      if (!teacher) {
          return res.status(403).json({ message: 'User is not registered as a teacher.' });
      }
      const teacherId = teacher.id; // This is the ID from the Teacher model, not the User ID
  
      // Check if the user is the teacher of this course (optional but recommended)
      const course = await Course.findByPk(courseId);
      if (!course) {
        return res.status(404).json({ message: 'Course not found.' });
      }
      // Assuming 'course.teacherId' refers to the ID in the Teacher model, not the User model ID
      if (course.teacherId !== teacherId) {
        return res.status(403).json({ message: 'You are not authorized to upload resources for this course.' });
      }
  
      const resource = await CourseResource.create({
        courseId,
        teacherId, // Use the teacher's ID from the Teacher model
        title,
        description,
        fileName: req.file.originalname,
        filePath: req.file.path,
        fileMimeType: req.file.mimetype,
        fileSize: req.file.size,
      });
  
      res.status(201).json({ message: 'Resource uploaded successfully', resource });
    } catch (error) {
      console.error('Error uploading course resource:', error);
      res.status(500).json({ message: 'Server error' });
    }
  };

  exports.getCourseResources = async (req, res) => {
    try {
      const { courseId } = req.params;
      const userId = req.user.id;
  
      const course = await Course.findByPk(courseId);
      if (!course) {
        return res.status(404).json({ message: 'Course not found.' });
      }
  
      const resources = await CourseResource.findAll({
        where: { courseId },
        order: [['uploadedAt', 'DESC']],
        attributes: ['id', 'title', 'description', 'fileName', 'fileMimeType', 'fileSize', 'uploadedAt'],
        include: [{
          model: Teacher, // Include the Teacher model
          attributes: ['id', 'designation'], // You might want designation
          include: [{
              model: User, // Then include the User model from Teacher to get the name
              attributes: ['name'] // Assuming 'name' is in the User model
          }]
        }]
      });
  
      const formattedResources = resources.map(resource => ({
          ...resource.toJSON(),
          teacherName: resource.Teacher ? resource.Teacher.User.name : 'Unknown' // Access name through Teacher.User
      }));
  
      res.status(200).json(formattedResources);
    } catch (error) {
      console.error('Error fetching course resources:', error);
      res.status(500).json({ message: 'Server error' });
    }
  };