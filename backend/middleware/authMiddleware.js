// backend/middleware/authMiddleware.js

const jwt = require('jsonwebtoken');
const db = require('../models'); // Assuming you import db from index.js
const User = db.User; // Access User model via db object

const authenticateUser = async (req, res, next) => {
    const token = req.header('Authorization')?.replace('Bearer ', '');

    console.log('[AUTH_MIDDLEWARE] Received Authorization Header:', req.header('Authorization'));
    console.log('[AUTH_MIDDLEWARE] Extracted Token:', token ? token.substring(0, 30) + '...' : 'None'); // Log partial token for security

    if (!token) {
        console.log('[AUTH_MIDDLEWARE] No token provided.');
        return res.status(401).json({ message: 'Access denied. No token provided.' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        console.log('[AUTH_MIDDLEWARE] Token successfully decoded:', decoded);

        const user = await User.findByPk(decoded.id);

        if (!user) {
            console.log('[AUTH_MIDDLEWARE] User not found for decoded ID:', decoded.id);
            return res.status(401).json({ message: 'Invalid token: User not found.' }); // More specific message
        }

        req.userId = user.id;
        req.user = user; // Attach the full user object
        console.log('[AUTH_MIDDLEWARE] User authenticated:', user.username, 'Role:', user.role);
        next();
    } catch (error) {
        console.error('[AUTH_MIDDLEWARE] Token verification failed:', error.message);
        // Check if error.name is 'TokenExpiredError' or 'JsonWebTokenError' for specific handling
        if (error.name === 'TokenExpiredError') {
            return res.status(401).json({ message: 'Invalid token: Token expired.' });
        } else if (error.name === 'JsonWebTokenError') {
             return res.status(401).json({ message: 'Invalid token: Malformed token or invalid secret.' });
        }
        return res.status(400).json({ message: 'Invalid token.' }); // Generic fallback
    }
};

module.exports = authenticateUser;