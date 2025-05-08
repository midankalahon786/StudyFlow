const express = require('express');
const router = express.Router();
const authenticateToken = require('../middleware/authMiddleware');
const { register, login, changePassword } = require('../controllers/authController');

//  Define the student and teacher registration routes
router.post('/register/student', async (req, res) => {
    try {
        //  Call the register function from authController, and pass the request and response objects
        await register(req, res);
    } catch (error) {
        console.error('Error in /register/student route:', error);
        res.status(500).json({ error: 'Internal server error during student registration' });
    }
});

router.post('/register/teacher', async (req, res) => {
    try {
        // Call the register function from authController, and pass the request and response objects
        await register(req, res);
    } catch (error) {
        console.error('Error in /register/teacher route:', error);
        res.status(500).json({ error: 'Internal server error during teacher registration' });
    }
});

// Login route
router.post('/login', async (req, res) => {
    try {
        await login(req, res);
    } catch (error) {
        console.error('Error in /login route:', error);
        res.status(500).json({ error: 'Internal server error during login' });
    }
});

router.post('/change-password', authenticateToken, async (req, res) => {
    try {
        await changePassword(req, res);
    } catch (error) {
        console.error('Error in /change-password route:', error);
        res.status(500).json({ error: 'Internal server error during password change' });
    }
});


module.exports = router;
