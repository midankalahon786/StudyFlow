// middleware/auth.js

const jwt = require('jsonwebtoken');
const User = require('../models/user'); // Adjust according to your User model location

const authenticateUser = async (req, res, next) => {
    const token = req.header('Authorization')?.replace('Bearer ', '');

    if (!token) {
        return res.status(401).json({ message: 'Access denied. No token provided.' });
    }

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        const user = await User.findByPk(decoded.id); // Assuming user ID is stored in JWT payload

        if (!user) {
            return res.status(401).json({ message: 'Invalid token.' });
        }

        // Attach the user's ID to req.userId
        req.userId = user.id;

        // You can still attach the entire user object if needed
        req.user = user;

        next();
    } catch (error) {
        return res.status(400).json({ message: 'Invalid token.' });
    }
};

module.exports = authenticateUser;