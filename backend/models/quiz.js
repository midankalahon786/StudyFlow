const { Sequelize, DataTypes } = require('sequelize');
const sequelize = require('./index');

const Quiz = sequelize.define('Quiz', {
    title: {
        type: DataTypes.STRING,
        allowNull: false,
    },
    timeLimit: {
        type: DataTypes.INTEGER,
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
        type: DataTypes.JSONB,
        allowNull: false,
    },
    createdBy: {
        type: DataTypes.INTEGER,
        allowNull: false,
    }
}, {
    timestamps: true,
});

module.exports = Quiz;
