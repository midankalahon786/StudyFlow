// backend/models/Event.js
// This file defines the Event model. It expects the sequelize instance
// and DataTypes object to be passed to it when it's required.

module.exports = (sequelize, DataTypes) => {
    const Event = sequelize.define('Event', {
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
            type: DataTypes.TEXT,
            allowNull: true,
        },
        // Type of event: e.g., 'quiz_deadline', 'assignment_due', 'lecture', 'personal_reminder'
        type: {
            type: DataTypes.ENUM('quiz_deadline', 'assignment_due', 'lecture', 'personal_reminder', 'other'),
            allowNull: false,
            defaultValue: 'other',
        },
        dueDate: { // The main deadline/event date
            type: DataTypes.DATE,
            allowNull: false,
        },
        // Optional: Date to trigger a reminder notification before the dueDate
        reminderDate: {
            type: DataTypes.DATE,
            allowNull: true,
        },
        // Foreign key to link to the Course if it's a course-specific event
        courseId: {
            type: DataTypes.UUID, // Assuming Course.id is UUID
            allowNull: true, // Can be null for personal events not tied to a specific course
            references: {
                model: 'Courses', // This refers to the actual table name of your Course model
                key: 'id',
            },
        },
        // Foreign key to link to the User who created/owns this event (e.g., teacher for lecture, student for personal)
        createdBy: {
            type: DataTypes.INTEGER, // Assuming User.id is INTEGER
            allowNull: false, // Events must have a creator
            references: {
                model: 'Users', // This refers to the actual table name of your User model
                key: 'id',
            },
        },
        // Optional: If the event is for a specific user (e.g., a personal reminder)
        // Or if it's a deadline for specific students in an assignment
        assignedToUserId: {
            type: DataTypes.INTEGER,
            allowNull: true,
            references: {
                model: 'Users',
                key: 'id',
            },
        },
        // Optional: To store details if this event is linked to a Quiz or Assignment
        quizId: {
            type: DataTypes.INTEGER, // Assuming Quiz.id is INTEGER
            allowNull: true,
            references: {
                model: 'Quizzes',
                key: 'id',
            },
        },
        assignmentId: {
            type: DataTypes.INTEGER, // Assuming Assignment.id is INTEGER (if you add an Assignment model)
            allowNull: true,
            // references: { // Will need this if Assignment model exists
            //     model: 'Assignments',
            //     key: 'id',
            // },
        },
    }, {
        timestamps: true, // Automatically adds createdAt and updatedAt columns
        tableName: 'Events' // Explicitly define the table name
    });

    return Event;
};