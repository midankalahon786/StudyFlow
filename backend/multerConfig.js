// backend/multerConfig.js
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Define the base upload directory (as in server.js)
const uploadsDir = path.join(__dirname, '..', 'uploads');
// Define the specific directory for course resources
const courseResourcesDir = path.join(uploadsDir, 'course_resources');

// Ensure the upload directory exists (redundant if server.js already does, but safe)
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir, { recursive: true });
}
if (!fs.existsSync(courseResourcesDir)) {
    fs.mkdirSync(courseResourcesDir, { recursive: true });
    console.log(`Created course resources upload directory: ${courseResourcesDir}`);
}

// Configure disk storage for Multer
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        // Files will be saved in the 'course_resources' subdirectory
        cb(null, courseResourcesDir);
    },
    filename: function (req, file, cb) {
        // Generate a unique filename: fieldname-timestamp.extension
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, `${file.fieldname}-${uniqueSuffix}${path.extname(file.originalname)}`);
    }
});

// Create the Multer upload instance
const upload = multer({
    storage: storage,
    limits: { fileSize: 20 * 1024 * 1024 }, // Increased to 20MB, adjust as needed
    fileFilter: (req, file, cb) => {
        // Accept common file types for resources
        const allowedTypes = /jpeg|jpg|png|gif|pdf|doc|docx|ppt|pptx|xls|xlsx|mp3|mp4|mov|avi|webm|txt|zip|rar/;
        const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
        const mimetype = allowedTypes.test(file.mimetype);

        if (extname && mimetype) {
            cb(null, true); // Accept the file
        } else {
            cb(new Error('Unsupported file type! Allowed types: images, PDFs, documents, audio, video, text, archives.'), false); // Reject the file
        }
    }
});

module.exports = upload;