const { DataTypes } = require('sequelize');
const sequelize = require('./index');

const Student = sequelize.define('Student', {
    enrollmentNumber: DataTypes.STRING,
    department: DataTypes.STRING,
    semester: DataTypes.STRING,
    batchYear: DataTypes.STRING
});

module.exports = Student;
