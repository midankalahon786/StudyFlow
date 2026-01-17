// F:\LMS\StudyFlow\backend\config\database.js
const { Sequelize } = require('sequelize');
require('dotenv').config();

const { PGDATABASE, PGUSER, PGPASSWORD, PGHOST } = process.env;
const sequelize = new Sequelize(PGDATABASE, PGUSER, PGPASSWORD, {
    host: PGHOST,
    dialect: 'postgres',
    // dialectOptions: { // Uncomment if using SSL on your PostgreSQL server
    //     ssl: {
    //         require: true,
    //         rejectUnauthorized: false
    //     }
    // }
});


module.exports = sequelize; // Export the sequelize instance directly