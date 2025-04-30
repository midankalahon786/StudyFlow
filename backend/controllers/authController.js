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
        // Ensure all required student fields are provided
        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !enrollmentNumber || !department || !semester || !batchYear) {
            return res.status(400).json({ error: 'Missing required fields for student registration' });
        }
        userData = { username, password, role, firstName, lastName, email, phoneNumber, enrollmentNumber, department, semester, batchYear };

    } else if (role === 'teacher') {
        const { username, password, firstName, lastName, email, phoneNumber, employeeId, department, designation, yearsOfExperience, qualifications } = req.body;
        // Ensure all required teacher fields are provided
        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !employeeId || !department || !designation || !yearsOfExperience || !qualifications) {
            return res.status(400).json({ error: 'Missing required fields for teacher registration' });
        }
        userData = { username, password, role, firstName, lastName, email, phoneNumber, employeeId, department, designation, yearsOfExperience, qualifications };
    } else {
        return res.status(400).json({ error: 'Invalid role.  Must be "student" or "teacher".' });
    }


    try {
        // Hash password before storing it in the database
        const hashedPassword = await bcrypt.hash(userData.password, 10);

        // Create a new user in the database
        const user = await User.create({
            ...userData,
            password: hashedPassword, // Override the password with the hashed version
        });

        // Respond with success message
        res.status(201).json({ message: 'User registered successfully' });
    } catch (err) {
        // Catch any errors and respond with a message
        console.error(res.status(400).json({ error: err.message }));
        res.status(400).json({ error: err.message });
    }
};

// Login user and generate JWT token (No changes needed here)
const login = async (req, res) => {
    const { username, password } = req.body;

    // Ensure username and password are provided
    if (!username || !password) {
        console.log(res.status(401).json({ error: 'Username and password are required' }));
        return res.status(401).json({ error: 'Username and password are required' });
    }

    try {
        // Find the user in the database by username
        const user = await User.findOne({ where: { username } });

        if (!user) {
            console.log(res.status(404).json({ error: 'User not found' }));
            return res.status(404).json({ error: 'User not found' });
        }

        // Compare the password with the stored hash
        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            console.log(res.status(410).json({ error: 'Invalid credentials' }));
            return res.status(410).json({ error: 'Invalid credentials' });
        }

        // Generate a JWT token with a payload containing the user id and role
        const token = jwt.sign(
            { id: user.id, role: user.role }, // Payload
            process.env.JWT_SECRET,        // Secret key (loaded from environment variables)
            { expiresIn: '1h' }            // Token expiration time
        );

        // Respond with the token and user details (excluding password)
        res.json({
            token,
            user: {
                id: user.id,
                username: user.username,
                role: user.role
            }
        });
    } catch (err) {
        console.error(res.status(500).json({ error: err.message }));
        res.status(500).json({ error: err.message });
    }
};

// Export register and login functions
module.exports = {
    register,
    login
};
