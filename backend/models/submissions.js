const { Sequelize, DataTypes } = require('sequelize');
const sequelize = require('./index');

const Submission = sequelize.define('Submission', {
    studentId: {
        type: DataTypes.INTEGER,
        allowNull: false,
    },
    quizId: {
        type: DataTypes.INTEGER,
        allowNull: false,
    },
    answers: {
        type: DataTypes.JSONB, // Store answers as JSON
        allowNull: false,
    },
    score: {
        type: DataTypes.FLOAT,
        allowNull: true,
    },
    submittedAt: {
        type: DataTypes.DATE,
        allowNull: false,
        defaultValue: Sequelize.NOW,
    },
}, {
    timestamps: true,
});

module.exports = Submission;
