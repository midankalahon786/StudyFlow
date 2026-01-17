const express = require('express');
const router = express.Router();
const eventController = require('../controllers/eventController');
const authMiddleware = require('../middleware/authMiddleware');
console.log('Type of authMiddleware:', typeof authMiddleware);

router.use(authMiddleware);
router.get('/', eventController.getEvents);
router.post('/', eventController.createEvent);
router.put('/:id', eventController.updateEvent);
router.delete('/:id', eventController.deleteEvent);
router.get('/upcoming-reminders', eventController.getUpcomingReminders);


module.exports = router;