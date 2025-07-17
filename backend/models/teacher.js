module.exports = (sequelize, DataTypes) => {
  const Teacher = sequelize.define('Teacher', {
      id: { 
          type: DataTypes.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
      },
      userId: { // Foreign key linking to User table
          type: DataTypes.INTEGER, // This must match the 'id' type of your User model
          allowNull: false,
          // A user can only be a teacher once (one-to-one relationship with User's teacher role)
          unique: true,
          // References within the model definition typically use the table name string
          references: {
              model: 'Users', // Reference to the Users table
              key: 'id'      // The primary key of the Users table
          }
      },
      employeeId: {
          type: DataTypes.STRING,
          allowNull: true, // Assuming this might be optional or assigned later
          unique: true // Employee IDs are usually unique
      },
      department: {
          type: DataTypes.STRING,
          allowNull: false // Assuming department is mandatory for a teacher
      },
      designation: {
          type: DataTypes.STRING,
          allowNull: false // Assuming designation is mandatory
      },
      yearsOfExperience: {
          type: DataTypes.INTEGER,
          allowNull: false,
          defaultValue: 0, // A sensible default if experience is 0 initially
      },
      qualifications: {
          type: DataTypes.TEXT, // Changed to TEXT for potentially longer lists of qualifications
          allowNull: false
      }
      // Removed the 'subjects' column definition
  }, {
      timestamps: true, // Automatically adds createdAt and updatedAt columns
      tableName: 'Teachers' // Explicitly define the table name
  });
  return Teacher; // IMPORTANT: Return the defined Teacher model
};