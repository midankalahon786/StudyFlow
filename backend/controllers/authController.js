const User = require('../models/user');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();

// Register new user
const register = async (req, res) => {
    const { role } = req.body;

    // Ensure role is provided
    if (!role) {
        return res.status(400).json({ error: 'Role is required' });
    }

    let userData;

    if (role === 'student') {
        const { username, password, firstName, lastName, email, phoneNumber, enrollmentNumber, department, semester, batchYear } = req.body;
        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !enrollmentNumber || !department || !semester || !batchYear) {
            return res.status(400).json({ error: 'Missing required fields for student registration' });
        }
        userData = { username, password, role, firstName, lastName, email, phoneNumber, enrollmentNumber, department, semester, batchYear };

    } else if (role === 'teacher') {
        const { username, password, firstName, lastName, email, phoneNumber, employeeId, department, designation, yearsOfExperience, qualifications } = req.body;
        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !employeeId || !department || !designation || !yearsOfExperience || !qualifications) {
            return res.status(400).json({ error: 'Missing required fields for teacher registration' });
        }
        userData = { username, password, role, firstName, lastName, email, phoneNumber, employeeId, department, designation, yearsOfExperience, qualifications };

    } else {
        return res.status(400).json({ error: 'Invalid role. Must be "student" or "teacher".' });
    }

    try {
        const hashedPassword = await bcrypt.hash(userData.password, 10);

        const user = await User.create({
            ...userData,
            password: hashedPassword,
        });

        res.status(201).json({ message: 'User registered successfully' });

    } catch (err) {
        console.error('Error during registration:', err);
        res.status(400).json({ error: err.message });
    }
};

// Login user and generate JWT token
const login = async (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.status(401).json({ error: 'Username and password are required' });
    }

    try {
        const user = await User.findOne({ where: { username } });

        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(410).json({ error: 'Invalid credentials' });
        }

        const token = jwt.sign(
            { id: user.id, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '1h' }
        );

        res.json({
            token,
            user: {
                id: user.id,
                username: user.username,
                role: user.role
            }
        });

    } catch (err) {
        console.error('Error during login:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = {
    register,
    login
};
