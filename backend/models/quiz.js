const { Sequelize, DataTypes } = require('sequelize');
const sequelize = require('./index');  // Assuming you have an established connection in index.js

const Quiz = sequelize.define('Quiz', {
    title: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    timeLimit: {
        type: DataTypes.INTEGER, // in seconds
        allowNull: false,
    },
    negativeMarking: {
        type: DataTypes.FLOAT,
        allowNull: false,
    },
    totalMarks: {
        type: DataTypes.FLOAT,
        allowNull: false,
    },
    questions: {
        type: DataTypes.JSONB, // Store questions as a JSON array
        allowNull: false,
    },
}, {
    timestamps: true,
});

module.exports = Quiz;
