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
