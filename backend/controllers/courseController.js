const Course = require('../models/course');

// Get all courses
exports.getCourses = async (req, res) => {
    try {
        const courses = await Course.findAll();
        res.json(courses);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Create new course
exports.createCourse = async (req, res) => {
    const { title, description } = req.body;
    try {
        const course = await Course.create({ title, description, createdBy: req.user.id });
        res.status(201).json(course);
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
};
