const { Sequelize } = require('sequelize');

const sequelize = new Sequelize('lmsdb', 'postgres', 'Mymonster@786', {
    host: 'localhost',
    dialect: 'postgres'
});

sequelize.sync(); // creates tables if not exist

module.exports = sequelize;
