const express = require('express');
const dotenv = require('dotenv');
const path = require('path');
const fs = require('fs');
const cors = require('cors');

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const uploadsDir = path.join(__dirname, 'uploads');
const courseResourcesDir = path.join(uploadsDir, 'course_resources');

if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir);
}
if (!fs.existsSync(courseResourcesDir)) {
  fs.mkdirSync(courseResourcesDir);
}

app.use('/uploads', express.static(uploadsDir));

const db = require('./models'); 
const authRoutes = require('./routes/auth');
const courseRoutes = require('./routes/course');
const quizRoutes = require('./routes/quiz');
const discussionRoutes = require('./routes/discussion');
const eventRoutes = require('./routes/event');
const aiRoutes = require('./routes/aiRoutes');
const cron = require('node-cron');
const eventController = require('./controllers/eventController');
const notificationsRouter = require('./routes/notifications');

app.use('/api/auth', authRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/quizzes', quizRoutes);
app.use('/api/discussion', discussionRoutes);
app.use('/api/calendar', eventRoutes);
app.use('/api/ai',aiRoutes);
app.use('/api/notifications', notificationsRouter);

app.use('/api/uploads/resources', (req, res, next) => {
  console.log('🪵 Static file requested:', req.originalUrl);
  next();
});

app.use('/api/uploads/resources', express.static(
  path.join(__dirname, '..', 'uploads', 'resources')
));

console.log('[STATIC MOUNT] Serving from:', path.join(__dirname, '..', 'uploads', 'resources'));

cron.schedule('* * * * *', () => { 
  console.log('Running reminder check job...');
  eventController.checkAndSendReminders();
});
console.log('Reminder cron job scheduled.');
const PORT = process.env.PORT || 5008;

db.sequelize.sync({ alter: true })
  .then(() => {
    console.log('Database & tables synced successfully!');
    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
      if (process.env.NODE_ENV !== 'production') {
        console.log('Server accessible at http://localhost:' + PORT);
      }
    });
  })
  .catch(err => {
    console.error('Error syncing database or starting server:', err);
    process.exit(1); 
  });

app.use((req, res, next) => {
  res.status(404).json({ message: 'Route not found' });
});

app.use((err, req, res, next) => {
  console.error(err.stack); 
  res.status(500).json({ message: 'Something went wrong', error: err.message });
});