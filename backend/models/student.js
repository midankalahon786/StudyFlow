const { DataTypes } = require('sequelize');
const sequelize = require('./index');

const Student = sequelize.define('Student', {
    enrollmentNumber: {
        type: DataTypes.STRING,
        allowNull: false
    },
    department: DataTypes.STRING,
    semester: DataTypes.STRING,
    batchYear: DataTypes.STRING,
    userId: {  // Foreign key linking to User table
        type: DataTypes.INTEGER,
        allowNull: false,
        references: {
            model: 'Users',  // Reference to the User model
            key: 'id'
        }
    }
});

module.exports = Student;
