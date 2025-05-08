// multerConfig.js
const multer = require('multer');
const path = require('path');

// Configure Multer to save files locally in the 'uploads' folder
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, './uploads');  // Files will be saved in the 'uploads' folder
  },
  filename: (req, file, cb) => {
    cb(null, Date.now() + path.extname(file.originalname));  // Unique filenames using timestamp
  }
});

const upload = multer({ storage: storage });

module.exports = upload;
