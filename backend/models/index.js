const { Sequelize } = require('sequelize');

require('dotenv').config(); // Load environment variables from .env file

const { PGDATABASE, PGUSER, PGPASSWORD, PGHOST } = process.env; // Destructure environment variables
const sequelize = new Sequelize(PGDATABASE, PGUSER, PGPASSWORD, {
    host: PGHOST,
    dialect: 'postgres',
    dialectOptions: {
        ssl: {
            require: true,
            rejectUnauthorized: false // This is important for Heroku
        }
    },
});

sequelize.sync(); // creates tables if not exist

module.exports = sequelize;
