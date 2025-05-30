const { Sequelize } = require('sequelize');

require('dotenv').config(); 

const { PGDATABASE, PGUSER, PGPASSWORD, PGHOST } = process.env; 
const sequelize = new Sequelize(PGDATABASE, PGUSER, PGPASSWORD, {
    host: PGHOST,
    dialect: 'postgres',
    dialectOptions: {
        ssl: {
            require: true,
            rejectUnauthorized: false 
        }
    }
});

sequelize.sync(); // creates tables if not exist

module.exports = sequelize;
