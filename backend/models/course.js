const { DataTypes } = require('sequelize');
const sequelize = require('./index');

const Course = sequelize.define('Course', {
  id: {
    type: DataTypes.INTEGER,
    primaryKey: true,
    autoIncrement: true,
    allowNull: false,
  },
  title: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  description: {
    type: DataTypes.STRING,
    allowNull: false,
  },
  fileUrl: {
    type: DataTypes.STRING,
    allowNull: true,
  },
  assignedUsers: {
    type: DataTypes.ARRAY(DataTypes.STRING),
    allowNull: false,
  },
  createdBy: {
    type: DataTypes.INTEGER, // changed from STRING to INTEGER
    allowNull: false,
  }
}, {
  timestamps: true,
});

module.exports = Course;
