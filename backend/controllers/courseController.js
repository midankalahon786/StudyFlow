const fs = require('fs').promises; // Use promise-based fs
const db = require('../models');
const { Resource, Teacher, Course, User } = db;
const ResourceType = require('../constants/resourceTypes');
const redisClient = require('../config/redis');

// --- HELPERS ---

/**
 * Safely deletes a file from the filesystem
 */
const deleteFile = async (filePath) => {
    if (!filePath) return;
    try {
        await fs.unlink(filePath);
    } catch (err) {
        console.error(`[FS_ERROR] Failed to delete: ${filePath}`, err.message);
    }
};

/**
 * Helper to fetch teacher by user ID
 */
const getTeacher = async (userId) => {
    return await Teacher.findOne({ where: { userId } });
};

// --- CONTROLLERS ---

exports.createCourseResource = async (req, res) => {
    try {
        const { courseId } = req.params;
        const { title, type, tags } = req.body;

        if (!title || !type) return res.status(400).json({ message: 'Title and type are required.' });
        if (!Object.values(ResourceType).includes(type)) return res.status(400).json({ message: 'Invalid type.' });

        const teacher = await getTeacher(req.user.id);
        if (!teacher) return res.status(403).json({ message: 'User is not a teacher.' });

        const fileData = req.file ? {
            fileName: req.file.filename,
            originalName: req.file.originalname,
            filePath: req.file.path,
            fileType: req.file.mimetype,
            fileSize: req.file.size
        } : {};

        // Requirement check for file-based types
        const fileTypes = [ResourceType.DOCUMENT, ResourceType.IMAGE, ResourceType.VIDEO, ResourceType.PRESENTATION, ResourceType.SPREADSHEET];
        if (fileTypes.includes(type) && !req.file) {
            return res.status(400).json({ message: 'This type requires a file upload.' });
        }

        const newResource = await Resource.create({
            ...req.body,
            ...fileData,
            courseId,
            teacherId: teacher.id,
            tags: tags ? tags.split(',').map(t => t.trim()) : null,
            uploadDate: new Date()
        });

        res.status(201).json({ message: 'Resource created', resource: newResource });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.getCourseResources = async (req, res) => {
    const { courseId } = req.params;
    const cacheKey = `resources:course:${courseId}`;

    try {
        // 1. Check Redis Cache
        const cachedData = await redisClient.get(cacheKey);
        if (cachedData) {
            return res.status(200).json(JSON.parse(cachedData));
        }

        // 2. Cache Miss: Fetch from SQL
        const resources = await db.Resource.findAll({
            where: { courseId },
            include: [{ model: db.Teacher, as: 'uploader' }]
        });

        // 3. Store in Redis for 1 Hour (3600 seconds)
        await redisClient.setEx(cacheKey, 3600, JSON.stringify(resources));

        res.status(200).json(resources);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.updateCourseResource = async (req, res) => {
    try {
        const resource = await Resource.findByPk(req.params.resourceId);
        const teacher = await getTeacher(req.user.id);

        if (!resource) return res.status(404).json({ message: 'Not found.' });
        if (resource.teacherId !== teacher?.id) return res.status(403).json({ message: 'Unauthorized.' });

        const updateData = { ...req.body };
        if (req.body.tags) updateData.tags = req.body.tags.split(',').map(t => t.trim());

        if (req.file) {
            await deleteFile(resource.filePath); // Remove old file
            updateData.fileName = req.file.filename;
            updateData.filePath = req.file.path;
            // ... add other file fields here
        }

        await resource.update(updateData);
        res.status(200).json({ message: 'Updated', resource });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.deleteCourseResource = async (req, res) => {
    try {
        const resource = await Resource.findByPk(req.params.resourceId);
        const teacher = await getTeacher(req.user.id);

        if (!resource) return res.status(404).json({ message: 'Not found.' });
        if (resource.teacherId !== teacher?.id) return res.status(403).json({ message: 'Unauthorized.' });

        await deleteFile(resource.filePath);
        await resource.destroy();

        res.status(200).json({ message: 'Resource deleted successfully.' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};