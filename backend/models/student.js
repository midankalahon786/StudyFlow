module.exports = (sequelize, DataTypes) => {
    const Student = sequelize.define('Student', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true,
            allowNull: false,
        },
        userId: {
            type: DataTypes.INTEGER, // This must match the 'id' type of your User model
            allowNull: false,
            unique: true, // A User can only be a Student once
            references: {
                model: 'Users', // This refers to the actual table name of your User model
                key: 'id',
            },
        },
        enrollmentNumber: {
            type: DataTypes.STRING,
            allowNull: false,
            unique: true, // Enrollment numbers are typically unique
        },
        department: {
            type: DataTypes.STRING,
            allowNull: false,
        },
        semester: {
            type: DataTypes.STRING,
            allowNull: false,
        },
        batchYear: {
            type: DataTypes.STRING,
            allowNull: false,
        },
    }, {
        timestamps: true, // Automatically adds createdAt and updatedAt columns
        tableName: 'Students' // Explicitly define the table name
    });

    return Student; // IMPORTANT: Return the defined Student model
};