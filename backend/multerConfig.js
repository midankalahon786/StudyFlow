const multer = require('multer');
const path = require('path');
const fs = require('fs'); // Node.js file system module

// Define the directory for storing uploads.
const UPLOAD_DIR = path.join(__dirname, '../uploads');
const RESOURCES_DIR = path.join(UPLOAD_DIR, 'resources');
const COURSE_FILES_DIR = path.join(UPLOAD_DIR, 'course_files'); // New directory for general course files

// Ensure the upload directories exist
if (!fs.existsSync(UPLOAD_DIR)) {
    fs.mkdirSync(UPLOAD_DIR);
}
if (!fs.existsSync(RESOURCES_DIR)) {
    fs.mkdirSync(RESOURCES_DIR);
}
if (!fs.existsSync(COURSE_FILES_DIR)) { // Ensure course_files directory exists
    fs.mkdirSync(COURSE_FILES_DIR);
}

// Storage configuration for course resources
const resourceStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        // Dynamic destination based on courseId
        const courseId = req.params.courseId; // Assuming courseId is in URL params
        const courseResourcesPath = path.join(RESOURCES_DIR, courseId);

        // Create course-specific directory if it doesn't exist
        if (!fs.existsSync(courseResourcesPath)) {
            fs.mkdirSync(courseResourcesPath, { recursive: true });
        }
        cb(null, courseResourcesPath);
    },
    filename: (req, file, cb) => {
        // Generate a unique filename to prevent overwrites
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});

// Multer upload middleware for resources
const resourceUpload = multer({
    storage: resourceStorage,
    limits: { fileSize: 10 * 1024 * 1024 }, // Limit file size to 10MB (adjust as needed)
    fileFilter: (req, file, cb) => {

        console.log('File upload attempt:', file.originalname, 'MIME:', file.mimetype);

        const allowedExts = ['.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.jpg', '.jpeg', '.png', '.mp4', '.mov', '.avi'];
        const allowedMimes = [
            'application/pdf',
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'application/vnd.ms-powerpoint',
            'application/vnd.openxmlformats-officedocument.presentationml.presentation',
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            'image/jpeg',
            'image/png',
            'video/mp4',
            'video/quicktime', // for .mov
            'video/x-msvideo'  // for .avi
        ];
    
        const ext = path.extname(file.originalname).toLowerCase();
        const mime = file.mimetype;
    
        if (allowedExts.includes(ext) || allowedMimes.includes(mime)) {
            return cb(null, true);
        } else {
            cb(new Error('Error: Only PDF, document, presentation, spreadsheet, image, or video files are allowed!'));
        }
    }
    
});


// New: Storage configuration for general course files (e.g., for createCourse)
const courseFileStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, COURSE_FILES_DIR); // Store in a general course_files directory
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
    }
});

// New: Multer upload middleware for general course files
const courseFileUpload = multer({
    storage: courseFileStorage,
    limits: { fileSize: 5 * 1024 * 1024 }, // Limit file size to 5MB (adjust as needed)
    fileFilter: (req, file, cb) => {
        const allowedTypes = /pdf|doc|docx|jpg|jpeg|png/; // Example: only documents and images
        const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
        const mimetype = allowedTypes.test(file.mimetype);

        if (extname && mimetype) {
            return cb(null, true);
        } else {
            cb(new Error('Error: Only PDF, document, or image files are allowed for course creation!'));
        }
    }
});


module.exports = {
    resourceUpload,
    courseFileUpload // Export the new course file upload middleware
};
