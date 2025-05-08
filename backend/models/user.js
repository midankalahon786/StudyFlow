const { DataTypes } = require('sequelize');
const sequelize = require('./index');
const Student = require('./student');  // Import the Student model
const Teacher = require('./teacher');  // Import the Teacher model

const User = sequelize.define('User', {
    username: {
        type: DataTypes.STRING,
        unique: true,
        allowNull: false
    },
    password: {
        type: DataTypes.STRING,
        allowNull: false
    },
    role: {
        type: DataTypes.ENUM('student', 'teacher', 'admin'),
        allowNull: false
    },
    firstName: DataTypes.STRING,
    lastName: DataTypes.STRING,
    email: DataTypes.STRING,
    phoneNumber: DataTypes.STRING
});

// After creating a user, create corresponding Student or Teacher
User.afterCreate(async (user, options) => {
    try {
        if (user.role === 'student') {
            // Create the Student record and associate it with the user
            await Student.create({
                userId: user.id,  // Foreign key that links to User
                enrollmentNumber: user.enrollmentNumber,  // Add other fields as needed
                department: user.department,
                semester: user.semester,
                batchYear: user.batchYear
            });
        }

        if (user.role === 'teacher') {
            // Create the Teacher record and associate it with the user
            await Teacher.create({
                userId: user.id,  // Foreign key that links to User
                department: user.department,
                subjects: user.subjects  // Add subjects or other fields for teachers
            });
        }
    } catch (error) {
        console.error('Error creating associated student/teacher:', error);
    }
});

module.exports = User;
