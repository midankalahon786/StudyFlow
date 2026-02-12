const { createClient } = require('redis');

const redisClient = createClient({
    url: 'redis://192.168.116.137:6379'
});

redisClient.on('error', (err) => console.error('Redis Client Error', err));
redisClient.on('connect', () => console.log('✅ Connected to Redis'));

(async () => {
    await redisClient.connect();
})();

module.exports = redisClient;