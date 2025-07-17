const express = require('express');
const dotenv = require('dotenv');
const path = require('path');
const fs = require('fs');
const cors = require('cors');

dotenv.config(); // Load environment variables FIRST

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const uploadsDir = path.join(__dirname, 'uploads');
const courseResourcesDir = path.join(uploadsDir, 'course_resources');

// Ensure upload directories exist
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir);
}
if (!fs.existsSync(courseResourcesDir)) {
  fs.mkdirSync(courseResourcesDir);
}

app.use('/uploads', express.static(uploadsDir));

const db = require('./models'); 


// Use API routes
const authRoutes = require('./routes/auth');
const courseRoutes = require('./routes/course');
const quizRoutes = require('./routes/quiz');
const discussionRoutes = require('./routes/discussion');
const eventRoutes = require('./routes/event');
const cron = require('node-cron');
const eventController = require('./controllers/eventController');

app.use('/api/auth', authRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/quizzes', quizRoutes);
app.use('/api/discussion', discussionRoutes);
app.use('/api/calendar', eventRoutes);
// Log file access BEFORE static middleware
app.use('/api/uploads/resources', (req, res, next) => {
  console.log('🪵 Static file requested:', req.originalUrl);
  next();
});
// Go up one level from backend to root
app.use('/api/uploads/resources', express.static(
  path.join(__dirname, '..', 'uploads', 'resources')
));

console.log('[STATIC MOUNT] Serving from:', path.join(__dirname, '..', 'uploads', 'resources'));

// Schedule cron job for reminders
cron.schedule('* * * * *', () => { // Runs every minute
  console.log('Running reminder check job...');
  // Ensure eventController.checkAndSendReminders() has access to models (via db object if needed)
  eventController.checkAndSendReminders();
});
console.log('Reminder cron job scheduled.');


const PORT = process.env.PORT || 5008;

// Sync database and then start the server
// This entire block should be the *only* place app.listen is called.
db.sequelize.sync({ alter: true }) // Adjust `alter: true` or `force: true` as needed for migrations
  .then(() => {
    console.log('Database & tables synced successfully!');
    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
      // Optional: log the server URL for local development
      if (process.env.NODE_ENV !== 'production') {
        console.log('Server accessible at http://localhost:' + PORT);
      }
    });
  })
  .catch(err => {
    console.error('Error syncing database or starting server:', err);
    process.exit(1); // Exit the process if database sync fails, as the app cannot function without it
  });


// Global error handler - place these AFTER all other routes
app.use((req, res, next) => {
  res.status(404).json({ message: 'Route not found' });
});

app.use((err, req, res, next) => {
  console.error(err.stack); // Log the full error stack for debugging
  res.status(500).json({ message: 'Something went wrong', error: err.message });
});