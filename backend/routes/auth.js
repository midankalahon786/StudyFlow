const express = require('express');
const router = express.Router();
const authenticateToken = require('../middleware/authMiddleware'); // Your main auth middleware
const authorizeRole = require('../middleware/authorizeRole'); // Middleware for role-based authorization
const db = require('../models'); // Import db object to access models directly (e.g., db.User)

const { register, login, changePassword, getAllUsers } = require('../controllers/authController');

// Define the adminOnly middleware instance here
const adminOnly = authorizeRole(['admin']);

// Define the student registration route
router.post('/register/student', async (req, res) => {
    console.log(`[AUTH ROUTE] POST /register/student received. Student is registered successfully.`); // Logging request
    try {
        await register(req, res);
    } catch (error) {
        console.error('Error in /register/student route:', error);
        res.status(500).json({ error: 'Internal server error during student registration' });
    }
});

router.post('/register/teacher', async (req, res) => {
    console.log(`[AUTH ROUTE] POST /register/teacher received. Teacher is registered successfully.`); // Logging request
    try {
        await register(req, res);
    } catch (error) {
        console.error('Error in /register/teacher route:', error);
        res.status(500).json({ error: 'Internal server error during teacher registration' });
    }
});

router.post('/register/admin', async (req, res) => {
    console.log(`[AUTH ROUTE] POST /register/admin received. Admin registered successfully.`); // Logging request
    try {
        await register(req, res);
    } catch (error) {
        console.error('Error in /register/admin route:', error);
        res.status(500).json({ error: 'Internal server error during admin registration' });
    }
});

// Login route
router.post('/login', async (req, res) => {
    console.log(`[AUTH ROUTE] POST /login received. Logged In`); // Logging request
    try {
        await login(req, res);
    } catch (error) {
        console.error('Error in /login route:', error);
        res.status(500).json({ error: 'Internal server error during login' });
    }
});

router.post('/change-password', authenticateToken, async (req, res) => {
    console.log(`[AUTH ROUTE] POST /change-password received. User ID: ${req.user ? req.user.id : 'N/A'}`); // Logging request, avoiding sensitive password in body
    try {
        await changePassword(req, res);
    } catch (error) {
        console.error('Error in /change-password route:', error);
        res.status(500).json({ error: 'Internal server error during password change' });
    }
});

// Route for fetching all users by an admin
router.get('/users', authenticateToken, adminOnly, async (req, res) => { // Added adminOnly middleware
    console.log(`[AUTH ROUTE] GET /users received by Admin User ID: ${req.user ? req.user.id : 'N/A'}`); // Logging request
    try {
        await getAllUsers(req, res); // Calls the controller function
    } catch (error) {
        console.error('Error in /users route:', error);
        res.status(500).json({ error: 'Internal server error fetching users' });
    }
});

// NEW/UPDATED: Admin route to update a user by ID
// This route is protected by authentication and role authorization (adminOnly)
router.put('/users/:id', authenticateToken, adminOnly, async (req, res) => {
    console.log(`[AUTH ROUTE] PUT /users/${req.params.id} received by Admin User ID: ${req.user ? req.user.id : 'N/A'}. Body:`, req.body);
    const userIdToUpdate = req.params.id; // User ID from URL parameters
    const { email, role, username, firstName, lastName, phoneNumber } = req.body; // Fields to update

    try {
        // Find the user by primary key (ID)
        const user = await db.User.findByPk(userIdToUpdate);
        if (!user) {
            return res.status(404).json({ message: 'User not found.' });
        }

        user.email = email !== undefined ? email : user.email;
        user.role = role !== undefined ? role : user.role;
        user.username = username !== undefined ? username : user.username;
        user.firstName = firstName !== undefined ? firstName : user.firstName;
        user.lastName = lastName !== undefined ? lastName : user.lastName;
        user.phoneNumber = phoneNumber !== undefined ? phoneNumber : user.phoneNumber;

        await user.save(); // Save the changes to the database

        res.status(200).json({ message: 'User updated successfully.', user: {
            id: user.id, username: user.username, email: user.email, role: user.role
        }}); // Return updated user details
    } catch (error) {
        console.error(`Error updating user ${userIdToUpdate} by admin:`, error);
        res.status(500).json({ error: 'Internal server error updating user: ' + error.message });
    }
});

router.delete('/users/:id', authenticateToken, adminOnly, async (req, res) => {
    console.log(`[AUTH ROUTE] DELETE /users/${req.params.id} received by Admin User ID: ${req.user ? req.user.id : 'N/A'}`);
    const userIdToDelete = req.params.id;

    if (req.user && req.user.id == userIdToDelete && req.user.role === 'admin') {
        return res.status(403).json({ message: "Forbidden: An admin cannot delete their own account." });
    }

    try {
        const user = await db.User.findByPk(userIdToDelete);
        if (!user) {
            return res.status(404).json({ message: 'User not found.' });
        }
        await user.destroy();
        res.status(200).json({ message: 'User deleted successfully.' });
    } catch (error) {
        console.error(`Error deleting user ${userIdToDelete} by admin:`, error);
        res.status(500).json({ error: 'Internal server error deleting user: ' + error.message });
    }
});


module.exports = router;