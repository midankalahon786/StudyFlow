// backend/models/User.js
// This file now exports a function that will define and return the User model.
// This allows sequelize and DataTypes to be passed dynamically from index.js.

module.exports = (sequelize, DataTypes) => {
    const User = sequelize.define('User', {
        id: {
            type: DataTypes.INTEGER,
            primaryKey: true,
            autoIncrement: true,
        },
        username: {
            type: DataTypes.STRING,
            unique: true,
            allowNull: false
        },
        password: {
            type: DataTypes.STRING,
            allowNull: false
        },
        role: {
            type: DataTypes.ENUM('student', 'teacher', 'admin'),
            allowNull: false,
            defaultValue: 'student' // Ensure a default if not set
        },
        firstName: DataTypes.STRING,
        lastName: DataTypes.STRING,
        email: { // Often email is unique and required
            type: DataTypes.STRING,
            unique: true,
            allowNull: true, // or false if required
            validate: {
                isEmail: true
            }
        },
        phoneNumber: DataTypes.STRING,
    }, {
        timestamps: true, 
        tableName: 'Users', 
        hooks: {
            afterCreate: async (user, options) => {
                console.log(`User ${user.username} with role ${user.role} created.`);
            
            }
        }
    });

    return User; 
};