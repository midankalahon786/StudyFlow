const express = require('express');
const router = express.Router();
const { getCourses, createCourse } = require('../controllers/courseController');
const authenticateToken = require('../middleware/authMiddleware');

router.get('/', authenticateToken, getCourses);
router.post('/', authenticateToken, createCourse);

module.exports = router;
