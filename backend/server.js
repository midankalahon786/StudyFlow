const express = require('express');
const sequelize = require('./models');
const authRoutes = require('./routes/auth');
const courseRoutes = require('./routes/course');
const quizRoutes = require('./routes/quiz'); 
require('dotenv').config();  // 🛠️ Load env variables FIRST

const app = express();
app.use(express.json());

// Use API routes
app.use('/api/auth', authRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/quizzes', quizRoutes);  // Add quiz routes

// Log only if needed (avoid logging secrets in production)
if (process.env.NODE_ENV !== 'production') {
  console.log('JWT_SECRET:', process.env.JWT_SECRET);
}

// Sync models with DB
sequelize.sync()
  .then(() => console.log('Database synchronized'))
  .catch((err) => console.error('Error syncing database:', err));

const PORT = process.env.PORT || 5008; // 🛠️ Optional: read PORT from .env too
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

// Optional: log the server URL
if (process.env.NODE_ENV !== 'production') {
  console.log('Server running at https://backend.r786.me');
}

// Global error handler (optional but recommended for production)
app.use((req, res, next) => {
  res.status(404).json({ message: 'Route not found' });
});

// Handle other errors globally
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: 'Something went wrong', error: err.message });
});
