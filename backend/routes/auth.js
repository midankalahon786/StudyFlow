const express = require('express');
const router = express.Router();
const { register, login } = require('../controllers/authController');

router.post('/register', async (req, res) => {
    try {
        await register(req, res);
    } catch (error) {
        console.error('Error in /register route:', error);
        res.status(500).json({ error: 'Internal server error during registration' });
    }
});

router.post('/login', async (req, res) => {
    try {
        await login(req, res);
    } catch (error) {
        console.error('Error in /login route:', error);
        res.status(500).json({ error: 'Internal server error during login' });
    }
});

module.exports = router;
