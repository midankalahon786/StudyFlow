// backend/routes/event.js
const express = require('express');
const router = express.Router();
const eventController = require('../controllers/eventController');
const authMiddleware = require('../middleware/authMiddleware'); // Your existing authentication middleware
console.log('Type of authMiddleware:', typeof authMiddleware);
// All event routes will require authentication
router.use(authMiddleware);

// Get all events for the authenticated user, optionally filtered by date range or course
router.get('/', eventController.getEvents);

// Create a new event
router.post('/', eventController.createEvent);

// Update an existing event
router.put('/:id', eventController.updateEvent);

// Delete an event
router.delete('/:id', eventController.deleteEvent);

// Get upcoming reminders (can be used by frontend for in-app popups, or by a backend worker)
router.get('/upcoming-reminders', eventController.getUpcomingReminders);


module.exports = router;