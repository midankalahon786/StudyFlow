

module.exports = (sequelize, DataTypes) => {
  const Course = sequelize.define('Course', {
      id: {
          type: DataTypes.UUID,        // Correct type for UUID
          defaultValue: DataTypes.UUIDV4, // This generates the UUID automatically
          primaryKey: true,            // Designates it as the primary key
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
      createdBy: {
          type: DataTypes.INTEGER, 
          allowNull: false,
      }
  }, {
      timestamps: true, 
      tableName: 'Courses' 
  });


  return Course; 
};