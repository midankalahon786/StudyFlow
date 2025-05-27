const User = require('../models/user');
const Student = require('../models/student');
const Teacher = require('../models/teacher');

const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();



const register = async (req, res) => {
    const { role } = req.body;

    if (!role) {
        return res.status(400).json({ error: 'Role is required' });
    }

    let userData;
    // Declare variables for student/teacher fields
    let enrollmentNumber, department, semester, batchYear;
    let employeeId, designation, yearsOfExperience, qualifications, subjects;

    if (role === 'student') {
        const {
            username, password, firstName, lastName,
            email, phoneNumber,
            enrollmentNumber: enr, department: dept, semester: sem, batchYear: batch
        } = req.body;

        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !enr || !dept || !sem || !batch) {
            return res.status(400).json({ error: 'Missing required fields for student registration' });
        }

        enrollmentNumber = enr;
        department = dept;
        semester = sem;
        batchYear = batch;

        userData = { username, password, role, firstName, lastName, email, phoneNumber };

    } else if (role === 'teacher') {
        const {
            username, password, firstName, lastName,
            email, phoneNumber,
            employeeId: empId, department: dept, designation: desg,
            yearsOfExperience: exp, qualifications: quals, subjects: subjs
        } = req.body;

        if (!username || !password || !firstName || !lastName || !email || !phoneNumber || !dept || !desg || !exp || !quals) {
            return res.status(400).json({ error: 'Missing required fields for teacher registration' });
        }

        employeeId = empId || null;
        department = dept;
        designation = desg;
        yearsOfExperience = exp;
        qualifications = quals;
        subjects = subjs || null;

        userData = { username, password, role, firstName, lastName, email, phoneNumber };
    } else {
        return res.status(400).json({ error: 'Invalid role. Must be "student" or "teacher".' });
    }

    try {
        const hashedPassword = await bcrypt.hash(userData.password, 10);

        let user = await User.create({
            ...userData,
            password: hashedPassword
        });

        if (role === 'student') {
            await Student.create({
                userId: user.id,
                enrollmentNumber,
                department,
                semester,
                batchYear
            });
        }

        if (role === 'teacher') {
            await Teacher.create({
                userId: user.id,
                employeeId,
                department,
                designation,
                yearsOfExperience,
                qualifications,
                subjects
            });
        }

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

const changePassword = async (req, res) => {
    const { oldPassword, newPassword } = req.body;
    const userId = req.user.id; // Assumes authMiddleware sets req.user

    if (!oldPassword || !newPassword) {
        return res.status(400).json({ error: 'Old and new passwords are required' });
    }

    try {
        const user = await User.findByPk(userId);
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        const isMatch = await bcrypt.compare(oldPassword, user.password);
        if (!isMatch) {
            return res.status(400).json({ error: 'Old password is incorrect' });
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        user.password = hashedPassword;
        await user.save();

        res.status(200).json({ message: 'Password updated successfully' });
    } catch (error) {
        console.error('Error changing password:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

module.exports = {
    register,
    login,
    changePassword
};
