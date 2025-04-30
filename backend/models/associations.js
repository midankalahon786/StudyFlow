const User = require('./user');
const Student = require('./student');
const Teacher = require('./Teacher');

// Define associations AFTER all models are imported
User.hasOne(Student, { foreignKey: 'userId', onDelete: 'CASCADE' });
User.hasOne(Teacher, { foreignKey: 'userId', onDelete: 'CASCADE' });

Student.belongsTo(User, { foreignKey: 'userId' });
Teacher.belongsTo(User, { foreignKey: 'userId' });

module.exports = { User, Student, Teacher };
