module.exports = (sequelize, DataTypes) => {
    const Comment = sequelize.define('Comment', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true,
        },
        content: {
            type: DataTypes.TEXT,
            allowNull: false,
        },
        userId: { // This column holds the foreign key
            type: DataTypes.INTEGER, // Assumed User.id is INTEGER
            allowNull: false,
        },
        courseId: { // This column holds the foreign key
            type: DataTypes.UUID, // <--- If Course.id is UUID
            // type: DataTypes.INTEGER, // <--- If Course.id is INTEGER
            allowNull: false,
        },
        parentId: { // For replies, self-referencing
            type: DataTypes.INTEGER,
            allowNull: true, // Can be null for top-level comments
        },
    }, {
        timestamps: true, // Adds createdAt and updatedAt
        tableName: 'Comments' // Explicit table name
    });
    return Comment;
};