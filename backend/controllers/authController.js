const User = require('../models/user');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();



// Register new user
const register = async (req, res) => {
    const { username, password, role } = req.body;

    // Ensure all fields are provided
    if (!username || !password || !role) {
        console.log(res.status(406).json({ error: 'Username, password, and role are required' }));
        return res.status(406).json({ error: 'Username, password, and role are required' });
    }

    try {
        // Hash password before storing it in the database
        const hashedPassword = await bcrypt.hash(password, 10);

        // Create a new user in the database
        const user = await User.create({ username, password: hashedPassword, role });

        // Respond with success message
        console.log(res.status(201).json({ message: 'User registered successfully' }));
    } catch (err) {
        // Catch any errors and respond with a message
        console.log(res.status(400).json({ error: err.message }));
    }
};

// Login user and generate JWT token
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
            process.env.JWT_SECRET, // Secret key (loaded from environment variables)
            { expiresIn: '1h' } // Token expiration time
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
