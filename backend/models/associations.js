const User = require('./user');
const Student = require('./student');
const Teacher = require('./teacher');
const Quiz = require('./quiz');
const Submission = require('./submissions');

// Define associations
User.hasOne(Student, { foreignKey: 'userId', onDelete: 'CASCADE' });
User.hasOne(Teacher, { foreignKey: 'userId', onDelete: 'CASCADE' });

Student.belongsTo(User, { foreignKey: 'userId' });
Teacher.belongsTo(User, { foreignKey: 'userId' });

Quiz.hasMany(Submission, { foreignKey: 'quizId', onDelete: 'CASCADE' });
Submission.belongsTo(Quiz, { foreignKey: 'quizId' });

module.exports = { User, Student, Teacher, Quiz, Submission };
