// models/teacher.js
const { DataTypes } = require('sequelize');
const sequelize = require('./index');

const Teacher = sequelize.define('Teacher', {
  userId: {  // Foreign key linking to User table
    type: DataTypes.INTEGER,
    allowNull: false,
    references: {
        model: 'Users',  // Reference to the User model
        key: 'id'
    }
},
  employeeId: {
    type: DataTypes.STRING,
    allowNull: true
  },
  department: {
    type: DataTypes.STRING,
    allowNull: false
  },
  designation: {
    type: DataTypes.STRING,
    allowNull: false
  },
  yearsOfExperience: {
    type: DataTypes.INTEGER,
    allowNull: false
  },
  qualifications: {
    type: DataTypes.STRING,
    allowNull: false
  },
  subjects: {
    type: DataTypes.STRING, // use JSON/ARRAY if storing multiple
    allowNull: true
  }
});

module.exports = Teacher;
