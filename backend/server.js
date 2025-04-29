const express = require('express');
const sequelize = require('./models');
const authRoutes = require('./routes/auth');
const courseRoutes = require('./routes/course');
const enrollmentRoutes = require('./routes/enrollment');
require('dotenv').config();  // 🛠️ Load env variables FIRST

const app = express();
app.use(express.json());

app.use('/api/auth', authRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/enrollments', enrollmentRoutes);

console.log('JWT_SECRET:', process.env.JWT_SECRET);

const PORT = process.env.PORT || 5008; // 🛠️ Optional: read PORT from .env too
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
