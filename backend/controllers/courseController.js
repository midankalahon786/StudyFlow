const path = require('path'); // Added path for file operations if needed
const fs = require('fs'); // Added fs for file system operations (e.g., deleting files)

// Assuming your models/index.js exports all models under a single 'db' object
const db = require('../models');
const Resource = db.Resource;
const Teacher = db.Teacher;
const User = db.User; // Assuming User model is also available via db
const Course = db.Course;

const ResourceType = require('../constants/resourceTypes');


// Get all courses
exports.getCourses = async (req, res) => {
    try {
        // Fetch all courses from the database
        const courses = await Course.findAll();
        res.json(courses); // Send courses as JSON
    } catch (err) {
        res.status(500).json({ error: err.message }); // Handle errors
    }
};

// Create new course
exports.createCourse = async (req, res) => {
    const { title, description, assignedUsers } = req.body;
    const file = req.file;

    // Validation for required fields
    if (!title || !description || !assignedUsers) {
        return res.status(400).json({ error: 'Title, description, and assigned users are required.' });
    }

    try {
        // Handle file upload URL (if file is uploaded)
        let fileUrl = null;
        if (file) {
            // Save the file URL to the course entry
            // The path will be relative to the 'uploads' directory served statically
            fileUrl = `/uploads/course_files/${file.filename}`;
        }
        const teacher = await Teacher.findOne({ where: { userId: req.user.id } });
        if (!teacher) return res.status(403).json({ message: 'Only teachers can create courses' });
        // Save course to the database
        const course = await Course.create({
            title,
            description,
            teacherId: teacher.id,
            assignedUsers: assignedUsers.split(',').map(user => user.trim()), // Convert comma-separated users into an array
            fileUrl,
            createdBy: req.user.id  // Assuming authentication middleware sets req.user.id
        });

        res.status(201).json(course); // Respond with the created course data
    } catch (err) {
        res.status(400).json({ error: err.message }); // Handle validation errors
    }
};

// New function to upload a resource to a course
exports.createCourseResource = async (req, res) => {
    console.log("📥 createCourseResource controller reached");
    console.log("📎 req.file =", req.file);
    console.log("📝 req.body =", req.body);

    try {
        const { courseId } = req.params;
        const { title, description, type, url, content, tags, friendlyType } = req.body;
        const userId = req.user.id;

        // ✅ Check required fields
        if (!title || !type) {
            return res.status(400).json({ message: 'Title and type are required.' });
        }

        // ✅ Validate type from allowed values
        const allowedTypes = Object.values(ResourceType);
        if (!allowedTypes.includes(type)) {
            console.warn("❌ Invalid resource type received:", type);
            return res.status(400).json({
                message: 'Invalid resource type provided.',
                validTypes: allowedTypes
            });
        }

        // ✅ Find teacher by userId
        const teacher = await Teacher.findOne({ where: { userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }

        const course = await Course.findByPk(courseId);
        if (!course) {
            return res.status(404).json({ message: 'Course not found.' });
        }

        // ✅ Handle file upload (optional)
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

        // ✅ Parse tags if present
        const parsedTags = tags ? tags.split(',').map(tag => tag.trim()) : null;

        // ✅ Create resource in DB
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
            
            // 🔥 Match the frontend structure
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
        const userId = req.user.id; // User ID from auth token

        // Get the teacher's ID from their userId
        const teacher = await Teacher.findOne({ where: { userId: userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }
        const teacherId = teacher.id; // Use the teacher's primary key (id)

        const resource = await Resource.findByPk(resourceId); // resourceId from params will be coerced to INTEGER

        if (!resource) {
            return res.status(404).json({ message: 'Resource not found.' });
        }

        // Ensure only the teacher who uploaded it (or an admin) can delete it
        if (resource.teacherId !== teacherId) {
            return res.status(403).json({ message: 'Forbidden: You are not authorized to delete this resource.' });
        }

        // Delete the file from the file system if it's a file resource
        if (resource.filePath) {
            fs.unlink(resource.filePath, (err) => {
                if (err) {
                    console.error('Error deleting file from filesystem:', err);
                    // Decide how to handle this error:
                    // 1. Return an error to the user immediately
                    // 2. Log and continue to delete DB entry (current behavior)
                    // For production, you might want more robust error handling/retry mechanisms.
                }
            });
        }

        // Delete the entry from the database
        await resource.destroy();
        res.status(200).json({ message: 'Resource deleted successfully.' });

    } catch (error) {
        console.error('Error deleting course resource:', error);
        res.status(500).json({ message: 'Server error deleting resource.', error: error.message });
    }
};

// NEW: Function to update a specific resource
exports.updateCourseResource = async (req, res) => {
    try {
        const { resourceId } = req.params;
        const { title, description, type, url, content, tags } = req.body;
        const userId = req.user.id;

        // Get the teacher's ID from their userId
        const teacher = await Teacher.findOne({ where: { userId: userId } });
        if (!teacher) {
            return res.status(403).json({ message: 'User is not registered as a teacher.' });
        }
        const teacherId = teacher.id;

        const resource = await Resource.findByPk(resourceId);

        if (!resource) {
            return res.status(404).json({ message: 'Resource not found.' });
        }

        // Ensure only the teacher who uploaded it can update it
        if (resource.teacherId !== teacherId) {
            return res.status(403).json({ message: 'Forbidden: You are not authorized to update this resource.' });
        }

        // Prepare update data
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


        // Handle file update if a new file is uploaded
        if (req.file) {
            // If an old file exists, delete it first
            if (resource.filePath) {
                fs.unlink(resource.filePath, (err) => {
                    if (err) {
                        console.error('Error deleting old file from filesystem:', err);
                        // Log but proceed with update, or handle more strictly
                    }
                });
            }
            updateData.fileName = req.file.filename;
            updateData.originalName = req.file.originalname;
            updateData.filePath = req.file.path;
            updateData.fileType = req.file.mimetype;
            updateData.fileSize = req.file.size;
        } else if (type === ResourceType.FILE && !req.file && !resource.filePath) {
            // If trying to change type to FILE but no file provided and no existing file
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