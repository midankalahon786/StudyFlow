const db = require('../models');
const { User, Student, Teacher } = db;
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Op } = require('sequelize');
require('dotenv').config();

/**
 * Helper: Role-specific data mapping and validation
 */
const ROLE_CONFIG = {
    student: {
        model: Student,
        fields: ['enrollmentNumber', 'department', 'semester', 'batchYear'],
        alias: 'studentProfile'
    },
    teacher: {
        model: Teacher,
        fields: ['employeeId', 'department', 'designation', 'yearsOfExperience', 'qualifications'],
        alias: 'teacherProfile'
    },
    admin: {
        model: null,
        fields: [],
        alias: null
    }
};

const register = async (req, res) => {
    const { role, username, password, email, firstName, lastName, phoneNumber } = req.body;

    // 1. Validate Basic Info & Role
    if (!role || !ROLE_CONFIG[role]) {
        return res.status(400).json({ error: 'Valid role is required' });
    }

    const baseFields = [username, password, email, firstName, lastName, phoneNumber];
    if (baseFields.some(field => !field)) {
        return res.status(400).json({ error: 'Missing core user fields' });
    }

    try {
        // 2. Conflict Check
        const existingUser = await User.findOne({
            where: { [Op.or]: [{ email }, { username }] }
        });
        if (existingUser) return res.status(409).json({ error: 'Email or username already exists' });

        // 3. Create Base User
        const hashedPassword = await bcrypt.hash(password, 10);
        const user = await User.create({
            username, email, firstName, lastName, phoneNumber, role,
            password: hashedPassword
        });

        // 4. Handle Role-Specific Profiles
        const config = ROLE_CONFIG[role];
        if (config.model) {
            const profileData = { userId: user.id };
            for (const field of config.fields) {
                profileData[field] = req.body[field] || (field === 'employeeId' ? null : undefined);
                
                // Strict validation for required profile fields (except employeeId)
                if (profileData[field] === undefined && field !== 'employeeId') {
                    await user.destroy(); // Rollback base user
                    return res.status(400).json({ error: `Missing field: ${field}` });
                }
            }
            await config.model.create(profileData);
        }

        res.status(201).json({ message: 'User registered successfully', userId: user.id, role });
    } catch (err) {
        console.error('Registration Error:', err);
        res.status(500).json({ error: 'Registration failed: ' + err.message });
    }
};

const login = async (req, res) => {
    const { username, password } = req.body;
    if (!username || !password) return res.status(400).json({ error: 'Credentials required' });

    try {
        const user = await User.findOne({ where: { username } });
        if (!user || !(await bcrypt.compare(password, user.password))) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        const token = jwt.sign(
            { id: user.id, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '1h' }
        );

        const { password: _, ...userSafe } = user.toJSON();
        res.json({ token, user: userSafe });
    } catch (err) {
        res.status(500).json({ error: 'Login error' });
    }
};

const getAllUsers = async (req, res) => {
    try {
        const users = await User.findAll({
            attributes: { exclude: ['password'] },
            include: [
                { model: Student, as: 'studentProfile', required: false },
                { model: Teacher, as: 'teacherProfile', required: false }
            ],
            order: [['createdAt', 'DESC']]
        });
        res.status(200).json(users);
    } catch (err) {
        res.status(500).json({ error: 'Fetch error' });
    }
};

module.exports = { register, login, getAllUsers };