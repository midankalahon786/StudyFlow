const path = require('path');
const fs = require('fs'); 

const db = require('../models');
const Resource = db.Resource;
const Teacher = db.Teacher;
const User = db.User; 
const Course = db.Course;
const ResourceType = require('../constants/resourceTypes');

exports.getCourses = async (req, res) => {
    try {
        const courses = await Course.findAll();
        res.json(courses); 
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createCourse = async (req, res) => {
    const { title, description, assignedUsers } = req.body;
    const file = req.file;

    if (!title || !description || !assignedUsers) {
        return res.status(400).json({ error: 'Title, description, and assigned users are required.' });
    }

    try {
        let fileUrl = null;
        if (file) {
            fileUrl = `/uploads/course_files/${file.filename}`;
        }
        const teacher = await Teacher.findOne({ where: { userId: req.user.id } });
        if (!teacher) return res.status(403).json({ message: 'Only teachers can create courses' });
        const course = await Course.create({
            title,
            description,
            teacherId: teacher.id,
            assignedUsers: assignedUsers.split(',').map(user => user.trim()),
            fileUrl,
            createdBy: req.user.id 
        });
        res.status(201).json(course); 
    } catch (err) {
        res.status(400).json({ error: err.message }); 
    }
};

exports.createCourseResource = async (req, res) => {
    console.log("📥 createCourseResource controller reached");
    console.log("📎 req.file =", req.file);
    console.log("📝 req.body =", req.body);

    try {
        const { courseId } = req.params;
        const { title, description, type, url, content, tags, friendlyType } = req.body;
        const userId = req.user.id;

        if (!title || !type) {
            return res.status(400).json({ message: 'Title and type are required.' });
        }
        const allowedTypes = Object.values(ResourceType);
        if (!allowedTypes.includes(type)) {
            console.warn("❌ Invalid resource type received:", type);
            return res.status(400).json({
                message: 'Invalid resource type provided.',
                validTypes: allowedTypes
            });
        }

        const teacher = await Teacher.findOne({ where: { userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }

        const course = await Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found.' });
        }

        let fileName = null;
        let originalName = null;
        let filePath = null;
        let fileType = null;
        let fileSize = null;

        if (req.file) {
            fileName = req.file.filename;
            originalName = req.file.originalname;
            filePath = req.file.path;
            fileType = req.file.mimetype;
            fileSize = req.file.size;
        } else if (type === ResourceType.DOCUMENT || type === ResourceType.IMAGE || type === ResourceType.VIDEO || type === ResourceType.PRESENTATION || type === ResourceType.SPREADSHEET) {
            return res.status(400).json({ message: 'File resource type requires a file upload.' });
        }
        const parsedTags = tags ? tags.split(',').map(tag => tag.trim()) : null;

        try {
            const newResource = await Resource.create({
                courseId,
                teacherId: teacher.id,
                title,
                description,
                type,
                friendlyType,
                url,
                content,
                tags: parsedTags,
                fileName,
                originalName,
                filePath,
                fileType,
                fileSize,
                uploadDate: new Date()
            });

            console.log("✅ Resource saved:", newResource.resourceId);
            return res.status(201).json({ message: 'Resource created successfully', resource: newResource });

        } catch (error) {
            console.error("❌ DB save error:", error);
            return res.status(500).json({ message: 'Failed to save resource to database.', error: error.message });
        }

    } catch (error) {
        console.error("❌ Error in createCourseResource:", error);
        return res.status(500).json({ message: 'Server error during resource creation.', error: error.message });
    }
};

exports.getCourseResources = async (req, res) => {
    try {
        const { courseId } = req.params;

        const course = await db.Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found.' });
        }

        const resources = await db.Resource.findAll({
            where: { courseId },
            order: [['uploadDate', 'DESC']],
            attributes: [
                'resourceId', 'title', 'description', 'type', 'friendlyType', 'url', 'content', 'tags',
                'fileName', 'fileType', 'fileSize', 'uploadDate','courseId'
            ],
            include: [{
                model: db.Teacher,
                as: 'uploader',
                attributes: ['id', 'designation'],
                include: [{
                    model: db.User,
                    as: 'user',
                    attributes: ['firstName', 'lastName']
                }]
            }]
        });

        const formatted = resources.map(resource => ({
            resourceId: resource.resourceId,
            title: resource.title,
            description: resource.description,
            courseId: resource.courseId,
            type: resource.type,
            friendlyType: resource.friendlyType,
            url: resource.url,
            content: resource.content,
            tags: resource.tags,
            fileName: resource.fileName,
            fileType: resource.fileType,
            fileSize: resource.fileSize,
            uploadDate: resource.uploadDate,

            uploadedBy: {
                username: `${resource.uploader?.user?.firstName || 'Unknown'} ${resource.uploader?.user?.lastName || ''}`.trim(),
                designation: resource.uploader?.designation || 'N/A',
                id: resource.uploader?.id || null
            }
        }));
        

        res.status(200).json(formatted);

    } catch (error) {
        console.error('Error fetching course resources:', error);
        res.status(500).json({ message: 'Server error fetching resources.', error: error.message });
    }
};

exports.deleteCourseResource = async (req, res) => {
    try {
        const { resourceId } = req.params;
        const userId = req.user.id; 

        const teacher = await Teacher.findOne({ where: { userId: userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }
        const teacherId = teacher.id; 
        const resource = await Resource.findByPk(resourceId); 

        if (!resource) {
            return res.status(404).json({ message: 'Resource not found.' });
        }

        if (resource.teacherId !== teacherId) {
            return res.status(403).json({ message: 'Forbidden: You are not authorized to delete this resource.' });
        }
        if (resource.filePath) {
            fs.unlink(resource.filePath, (err) => {
                if (err) {
                    console.error('Error deleting file from filesystem:', err);
                }
            });
        }
        await resource.destroy();
        res.status(200).json({ message: 'Resource deleted successfully.' });

    } catch (error) {
        console.error('Error deleting course resource:', error);
        res.status(500).json({ message: 'Server error deleting resource.', error: error.message });
    }
};

exports.updateCourseResource = async (req, res) => {
    try {
        const { resourceId } = req.params;
        const { title, description, type, url, content, tags } = req.body;
        const userId = req.user.id;

        const teacher = await Teacher.findOne({ where: { userId: userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }
        const teacherId = teacher.id;

        const resource = await Resource.findByPk(resourceId);

        if (!resource) {
            return res.status(404).json({ message: 'Resource not found.' });
        }

        if (resource.teacherId !== teacherId) {
            return res.status(403).json({ message: 'Forbidden: You are not authorized to update this resource.' });
        }

        const updateData = {};
        if (title !== undefined) updateData.title = title;
        if (description !== undefined) updateData.description = description;
        if (type !== undefined) {
            if (!Object.values(ResourceType).includes(type)) {
                return res.status(400).json({ message: 'Invalid resource type provided for update.' });
            }
            updateData.type = type;
        }
        if (url !== undefined) updateData.url = url;
        if (content !== undefined) updateData.content = content;
        if (tags !== undefined) updateData.tags = tags ? tags.split(',').map(tag => tag.trim()) : null;

        if (req.file) {
            if (resource.filePath) {
                fs.unlink(resource.filePath, (err) => {
                    if (err) {
                        console.error('Error deleting old file from filesystem:', err);
                    }
                });
            }
            updateData.fileName = req.file.filename;
            updateData.originalName = req.file.originalname;
            updateData.filePath = req.file.path;
            updateData.fileType = req.file.mimetype;
            updateData.fileSize = req.file.size;
        } else if (type === ResourceType.FILE && !req.file && !resource.filePath) {
            return res.status(400).json({ message: 'File resource type requires a file upload.' });
        }

        await resource.update(updateData);

        res.status(200).json({
            message: 'Resource updated successfully!',
            resource: resource
        });

    } catch (error) {
        console.error('Error updating course resource:', error);
        res.status(500).json({ message: 'Server error updating resource.', error: error.message });
    }
};