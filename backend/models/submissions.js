// F:\LMS\StudyFlow\backend\models\submissions.js

// This file now exports a function that will define and return the Submission model.
// It accepts `sequelize` and `DataTypes` as arguments from `index.js`.

module.exports = (sequelize, DataTypes) => {
    const Submission = sequelize.define('Submission', {
        id: { // Explicitly defining the primary key for Submission
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true,
            allowNull: false,
        },
        studentId: {
            type: DataTypes.INTEGER, // This must match the 'id' type of your Student model
            allowNull: false,
            // References within the model definition typically use the table name string
            references: {
                model: 'Students', // Reference to the Students table
                key: 'id'
            }
        },
        quizId: {
            type: DataTypes.INTEGER, // This must match the 'id' type of your Quiz model
            allowNull: false,
            // References within the model definition typically use the table name string
            references: {
                model: 'Quizzes', // Reference to the Quizzes table
                key: 'id'
            }
        },
        answers: {
            type: DataTypes.JSONB, // Store answers as JSON object or array
            allowNull: false,
        },
        score: {
            type: DataTypes.FLOAT,
            allowNull: true, // Score might be null if not yet graded
        },
        submittedAt: {
            type: DataTypes.DATE,
            allowNull: false,
            defaultValue: DataTypes.NOW, // Use DataTypes.NOW instead of Sequelize.NOW
        },
    }, {
        timestamps: true, // Automatically adds createdAt and updatedAt columns
        tableName: 'Submissions' // Explicitly define the table name
    });

    // Associations for Submission should be defined in backend/models/index.js, for example:
    // Submission.belongsTo(models.Student, { foreignKey: 'studentId', as: 'student' });
    // Submission.belongsTo(models.Quiz, { foreignKey: 'quizId', as: 'quiz' });

    return Submission; // IMPORTANT: Return the defined Submission model
};