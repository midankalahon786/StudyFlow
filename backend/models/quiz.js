
module.exports = (sequelize, DataTypes) => {
    const Quiz = sequelize.define('Quiz', {
        id: { // Assuming 'id' is implicit or you want to define it explicitly
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true,
            allowNull: false,
        },
        title: {
            type: DataTypes.STRING,
            allowNull: false,
        },
        timeLimit: {
            type: DataTypes.INTEGER, // Time in minutes or seconds, depending on your app logic
            allowNull: false,
        },
        negativeMarking: {
            type: DataTypes.FLOAT,
            allowNull: false,
            defaultValue: 0.0 // It's good practice to set a default if 0 is a common value
        },
        totalMarks: {
            type: DataTypes.FLOAT,
            allowNull: false,
        },
        questions: {
            // DataTypes.JSONB is excellent for storing an array of question objects
            type: DataTypes.JSONB,
            allowNull: false,
        },
        createdBy: {
            type: DataTypes.INTEGER, // This should match the 'id' type of your User model
            allowNull: false,
            // As discussed, foreign key references are often in index.js,
            // but can be declared here using the table name string for schema creation.
            references: {
                model: 'Users', // Use the string name of the target table
                key: 'id',
            },
        }
    }, {
        timestamps: true, // This automatically adds createdAt and updatedAt columns
        tableName: 'Quizzes' // Explicitly define the table name
    });

    // Associations for Quiz should be defined in backend/models/index.js, for example:
    // Quiz.belongsTo(models.Course, { foreignKey: 'courseId', as: 'course' });
    // Quiz.hasMany(models.Submission, { foreignKey: 'quizId', as: 'submissions' });
    // Quiz.belongsTo(models.User, { foreignKey: 'createdBy', as: 'creator' });


    return Quiz; // IMPORTANT: Return the defined Quiz model
};