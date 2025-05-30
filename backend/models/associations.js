const User = require('./User');
const Student = require('./student');
const Teacher = require('./teacher');
const Quiz = require('./quiz');
const Submission = require('./submissions');
const Course = require('./course'); 

User.hasOne(Student, { foreignKey: 'userId', onDelete: 'CASCADE' });
User.hasOne(Teacher, { foreignKey: 'userId', onDelete: 'CASCADE' });

Student.belongsTo(User, { foreignKey: 'userId' });
Teacher.belongsTo(User, { foreignKey: 'userId' });

Quiz.hasMany(Submission, { foreignKey: 'quizId', onDelete: 'CASCADE' });
Submission.belongsTo(Quiz, { foreignKey: 'quizId', as: 'quiz' });

// Define the many-to-many association between Course and Student
Course.belongsToMany(Student, {
  through: 'CourseStudent', 
  as: 'students',          
  foreignKey: 'courseId'
});

Student.belongsToMany(Course, {
  through: 'CourseStudent', 
  as: 'courses',         
  foreignKey: 'studentId'
});

Submission.belongsTo(Student, { foreignKey: 'studentId', as: 'student' });


module.exports = { User, Student, Teacher, Quiz, Submission, Course }; // Make sure to export Course