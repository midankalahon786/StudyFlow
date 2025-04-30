const { DataTypes } = require('sequelize');
const sequelize = require('./index');

const Teacher = sequelize.define('Teacher', {
    employeeId: DataTypes.STRING,
    designation: DataTypes.STRING,
    yearsOfExperience: DataTypes.INTEGER,
    qualifications: DataTypes.STRING
});

module.exports = Teacher;
