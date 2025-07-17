module.exports = (sequelize, DataTypes) => {
  return sequelize.define('Resource', {
    resourceId: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    courseId: {
      type: DataTypes.UUID,
      allowNull: false,
      references: {
        model: 'Courses',
        key: 'id'
      },
      onDelete: 'CASCADE',
      onUpdate: 'CASCADE'
    },
    teacherId: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'Teachers',
        key: 'id'
      },
      onDelete: 'CASCADE',
      onUpdate: 'CASCADE'
    },
    title: {
      type: DataTypes.STRING,
      allowNull: false
    },
    description: {
      type: DataTypes.TEXT,
      allowNull: true
    },
    type: {
      type: DataTypes.STRING,
      allowNull: false
    },
    friendlyType: {
      type: DataTypes.STRING,
      allowNull: true

    },
    fileName: {
      type: DataTypes.STRING,
      allowNull: false
    },
    originalName: {
      type: DataTypes.STRING,
      allowNull: false
    },
    filePath: {
      type: DataTypes.STRING,
      allowNull: false
    },
    fileType: {
      type: DataTypes.STRING,
      allowNull: false
    },
    fileSize: {
      type: DataTypes.INTEGER,
      allowNull: false
    },
    uploadDate: {
      type: DataTypes.DATE,
      allowNull: false
    },
    url:{
      type: DataTypes.STRING,
      allowNull:true
    },
    content:{
      type: DataTypes.TEXT,
      allowNull:true
    },
    tags: {
      type: DataTypes.ARRAY(DataTypes.STRING), // or DataTypes.STRING if stored as CSV
      allowNull: true
    }
  });
};
