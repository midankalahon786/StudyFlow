module.exports = (sequelize, DataTypes) => {
    const Notification = sequelize.define('Notification', {
      id: {
        type: DataTypes.UUID,
        defaultValue: DataTypes.UUIDV4,
        primaryKey: true,
        allowNull: false,
      },
      userId: {
        type: DataTypes.INTEGER, // Use UUID to match the user's primary key type
        allowNull: false,
        references: {
          model: 'Users', // This should be the name of your Users model/table
          key: 'id',       // This should be the name of the primary key in the Users model
        },
      },
      title: {
        type: DataTypes.STRING,
        allowNull: false,
      },
      message: {
        type: DataTypes.TEXT, // Use TEXT for potentially longer messages
        allowNull: true,
      },
      courseId: {
        type: DataTypes.UUID, // Use UUID to match the Course model's primary key
        allowNull: true,
        references: {
          model: 'Courses', // This should be the name of your Courses model/table
          key: 'id',       // This should be the name of the primary key in the Courses model
        },
      },
      notificationType: {
        type: DataTypes.STRING, // e.g., 'ASSIGNMENT', 'ANNOUNCEMENT', 'GRADE'
        allowNull: false,
      },
      isRead: {
        type: DataTypes.BOOLEAN,
        defaultValue: false,
        allowNull: false,
      },
    }, {
      timestamps: true,
      tableName: 'Notifications' // Recommended to use a consistent table name
    });
  
    // Define the associations
    Notification.associate = (models) => {
      Notification.belongsTo(models.User, {
        foreignKey: 'userId',
        onDelete: 'CASCADE',
      });
      Notification.belongsTo(models.Course, {
        foreignKey: 'courseId',
        onDelete: 'SET NULL', // Or 'CASCADE' if deleting a course should delete its notifications
      });
    };
  
    return Notification;
  };