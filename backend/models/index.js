// backend/models/index.js (The correct and complete version)

const fs = require('fs');
const path = require('path');
const Sequelize = require('sequelize'); // Import Sequelize itself and DataTypes
const basename = path.basename(__filename);
const env = process.env.NODE_ENV || 'development';

// Ensure your config.json path is correct relative to the backend directory
// Use path.join to ensure correct path resolution across OS
const config = require(path.join(__dirname, '/../config/config.json'))[env];
const db = {}; // This is the central object that will hold all your initialized models

let sequelize;
if (config.use_env_variable) {
  sequelize = new Sequelize(process.env[config.use_env_variable], config);
} else {
  sequelize = new Sequelize(config.database, config.username, config.password, {
    host: config.host,
    dialect: config.dialect,
    logging: config.logging || false, // Use logging from config or default to false
    pool: {
      max: 5,
      min: 0,
      acquire: 30000,
      idle: 10000
    },
    // Add dialectOptions only if SSL is explicitly required and configured in config.json
    ...(config.dialectOptions && config.dialectOptions.ssl && {
      dialectOptions: {
        ssl: {
          require: config.dialectOptions.ssl.require,
          rejectUnauthorized: config.dialectOptions.ssl.rejectUnauthorized
        }
      }
    })
  });
}

fs
  .readdirSync(__dirname)
  .filter(file => {
    // Filter out index.js itself and only include .js files
    return (file.indexOf('.') !== 0) && (file !== basename) && (file.slice(-3) === '.js');
  })
  .forEach(file => {
    console.log("Loading model:", file);
    const model = require(path.join(__dirname, file))(sequelize, Sequelize.DataTypes);
    db[model.name] = model;
  });


Object.keys(db).forEach(modelName => {
  if (db[modelName].associate) {
    db[modelName].associate(db);
  }
});

// User Associations
db.User.hasOne(db.Student, { foreignKey: 'userId', as: 'studentProfile', onDelete: 'CASCADE' });
db.Student.belongsTo(db.User, { foreignKey: 'userId' });

db.User.hasOne(db.Teacher, { foreignKey: 'userId', as: 'teacherProfile', onDelete: 'CASCADE' });
db.Teacher.belongsTo(db.User, { foreignKey: 'userId', as: 'user' }); 


// Add associations for User creating Quizzes or Courses if they exist
db.User.hasMany(db.Quiz, { foreignKey: 'createdBy', as: 'createdQuizzes' });
db.Quiz.belongsTo(db.User, { foreignKey: 'createdBy', as: 'creator' });

db.User.hasMany(db.Course, { foreignKey: 'createdBy', as: 'createdCourses' });
db.Course.belongsTo(db.User, { foreignKey: 'createdBy', as: 'creator' });


// Course Associations
db.Course.belongsToMany(db.Student, {
  through: 'CourseStudent', // This is the join table name
  as: 'students', // Alias for students in a course
  foreignKey: 'courseId'
});
db.Student.belongsToMany(db.Course, {
  through: 'CourseStudent',
  as: 'courses', // Alias for courses a student is in
  foreignKey: 'studentId'
});

db.Course.belongsTo(db.Teacher, { foreignKey: 'teacherId', as: 'teacher' });
db.Teacher.hasMany(db.Course, { foreignKey: 'teacherId', as: 'coursesTaught' });

db.Course.hasMany(db.Quiz, { foreignKey: 'courseId', as: 'quizzes' });
db.Quiz.belongsTo(db.Course, { foreignKey: 'courseId', as: 'course' });

//Resource Associations (Corrected again)
db.Course.hasMany(db.Resource, { foreignKey: 'courseId', as: 'resources'});
db.Resource.belongsTo(db.Course, { foreignKey: 'courseId', as: 'course'}); // Added 'as' alias for consistency

db.Teacher.hasMany(db.Resource, { foreignKey: 'teacherId', as: 'uploadedResources'});
db.Resource.belongsTo(db.Teacher, { foreignKey: 'teacherId', as: 'uploader'}); // Added 'as' alias for consistency


// Quiz Associations
// If Quiz has questions
// db.Quiz.hasMany(db.Question, { foreignKey: 'quizId', as: 'questions' });
// db.Question.belongsTo(db.Quiz, { foreignKey: 'quizId', as: 'quiz' });

db.Quiz.hasMany(db.Submission, { foreignKey: 'quizId', onDelete: 'CASCADE' });
db.Submission.belongsTo(db.Quiz, { foreignKey: 'quizId', as: 'quiz' }); // Submission belongs to a Quiz

db.Submission.belongsTo(db.Student, { foreignKey: 'studentId', as: 'student' }); // Submission belongs to a Student
db.Student.hasMany(db.Submission, { foreignKey: 'studentId', as: 'submissions' }); // Student has many Submissions


// Comment Associations
db.User.hasMany(db.Comment, { foreignKey: 'userId', as: 'userComments' });
db.Comment.belongsTo(db.User, { foreignKey: 'userId', as: 'author' });

db.Course.hasMany(db.Comment, { foreignKey: 'courseId', as: 'courseComments' });
db.Comment.belongsTo(db.Course, { foreignKey: 'courseId', as: 'course' });

// Self-referencing association for replies
db.Comment.hasMany(db.Comment, { foreignKey: 'parentId', as: 'replies' });
db.Comment.belongsTo(db.Comment, { foreignKey: 'parentId', as: 'parentComment' });


// Event Associations
db.Event.belongsTo(db.User, { foreignKey: 'createdBy', as: 'creator' });
db.User.hasMany(db.Event, { foreignKey: 'createdBy', as: 'createdEvents' });

db.Event.belongsTo(db.User, { foreignKey: 'assignedToUserId', as: 'assignedTo' });
db.User.hasMany(db.Event, { foreignKey: 'assignedToUserId', as: 'assignedEvents' });

db.Event.belongsTo(db.Course, { foreignKey: 'courseId', as: 'course' });
db.Course.hasMany(db.Event, { foreignKey: 'courseId', as: 'courseEvents' });

db.Event.belongsTo(db.Quiz, { foreignKey: 'quizId', as: 'quiz' });
db.Quiz.hasMany(db.Event, { foreignKey: 'quizId', as: 'quizEvents' });


// --- END CRITICAL SECTION ---


db.sequelize = sequelize; // Export the sequelize instance
db.Sequelize = Sequelize; // Export Sequelize library as well (for DataTypes etc.)

// IMPORTANT: Export the entire db object, which contains all initialized models
module.exports = db;