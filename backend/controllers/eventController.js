// backend/controllers/eventController.js
const { Event, User, Course, Quiz } = require('../models'); // Destructure models
const { Op } = require('sequelize'); // For date range queries

// Helper function for sending notifications (conceptual/placeholder)
const sendNotification = async (targetUserId, title, body, data = {}) => {
    // In a real application, this would send a push notification via FCM or similar.
    // This function would typically:
    // 1. Get the FCM token(s) for targetUserId from a UserDeviceTokens model/table.
    // 2. Use the Firebase Admin SDK to send a message to those tokens.
    console.log(`[NOTIFICATION_SIMULATED] Sending to User ${targetUserId}: "${title}" - "${body}"`, data);

    // Example Firebase Admin SDK structure (conceptual):
    /*
    const admin = require('firebase-admin');
    // Ensure Firebase Admin SDK is initialized somewhere (e.g., server.js or a config file)
    // const serviceAccount = require('./path/to/your/serviceAccountKey.json');
    // if (!admin.apps.length) {
    //   admin.initializeApp({
    //     credential: admin.credential.cert(serviceAccount)
    //   });
    // }

    const message = {
        notification: {
            title: title,
            body: body
        },
        data: {
            // Custom data for your app to handle (e.g., navigate to event details)
            eventType: data.type || 'generic',
            eventId: data.eventId ? String(data.eventId) : undefined,
            courseId: data.courseId ? String(data.courseId) : undefined,
            ...data
        },
        token: 'USER_FCM_TOKEN_HERE' // This would come from your DB for targetUserId
    };

    try {
        const response = await admin.messaging().send(message);
        console.log('Successfully sent message:', response);
    } catch (error) {
        console.error('Error sending message:', error);
    }
    */
};

// @route GET /api/calendar/events
// @desc Get all events for the authenticated user, possibly within a date range
// @access Private (requires authentication)
exports.getEvents = async (req, res) => {
    try {
        const userId = req.user.id; // Authenticated user's ID
        const { startDate, endDate, courseId } = req.query;

        let whereClause = {
            [Op.or]: [ // Events created by the user OR assigned to the user
                { createdBy: userId },
                { assignedToUserId: userId }
            ]
        };

        if (startDate && endDate) {
            whereClause.dueDate = {
                [Op.between]: [new Date(startDate), new Date(endDate)]
            };
        }
        if (courseId) {
            whereClause.courseId = courseId;
        }

        const events = await Event.findAll({
            where: whereClause,
            include: [
                { model: User, as: 'creator', attributes: ['id', 'username'] },
                { model: User, as: 'assignedTo', attributes: ['id', 'username'] },
                { model: Course, as: 'course', attributes: ['id', 'title'] },
                { model: Quiz, as: 'quiz', attributes: ['id', 'title'] }
            ],
            order: [['dueDate', 'ASC']],
        });
        res.status(200).json(events);
    } catch (error) {
        console.error('Error fetching events:', error);
        res.status(500).json({ message: 'Error fetching events', error: error.message });
    }
};

// @route POST /api/calendar/events
// @desc Create a new event/deadline
// @access Private (requires authentication, teachers/admins can create course events)
exports.createEvent = async (req, res) => {
    try {
        const { title, description, type, dueDate, reminderDate, courseId, assignedToUserId, quizId, assignmentId } = req.body;
        const createdBy = req.user.id; // Authenticated user is the creator

        if (!title || !type || !dueDate) {
            return res.status(400).json({ message: 'Title, type, and due date are required for an event.' });
        }

        // Basic validation for dates
        const parsedDueDate = new Date(dueDate);
        if (isNaN(parsedDueDate.getTime())) {
            return res.status(400).json({ message: 'Invalid due date format.' });
        }
        const parsedReminderDate = reminderDate ? new Date(reminderDate) : null;
        if (parsedReminderDate && isNaN(parsedReminderDate.getTime())) {
             return res.status(400).json({ message: 'Invalid reminder date format.' });
        }

        const newEvent = await Event.create({
            title, description, type, dueDate: parsedDueDate,
            reminderDate: parsedReminderDate, courseId, createdBy,
            assignedToUserId, quizId, assignmentId
        });

        // Optionally, send a notification immediately if reminderDate is near or for confirmation
        // For real-time, scheduled notifications, see "Notification Scheduling" section below.

        res.status(201).json(newEvent);
    } catch (error) {
        console.error('Error creating event:', error);
        res.status(500).json({ message: 'Error creating event', error: error.message });
    }
};

// @route PUT /api/calendar/events/:id
// @desc Update an existing event
// @access Private (requires authentication and authorization - only creator or admin)
exports.updateEvent = async (req, res) => {
    try {
        const eventId = req.params.id;
        const userId = req.user.id;
        const userRole = req.user.role; // Assuming role is available on req.user

        const { title, description, type, dueDate, reminderDate, courseId, assignedToUserId, quizId, assignmentId } = req.body;

        const event = await Event.findByPk(eventId);
        if (!event) {
            return res.status(404).json({ message: 'Event not found.' });
        }

        // Authorization: Only the creator or an admin can update
        if (event.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized to update this event.' });
        }

        // Basic validation for dates if provided
        const parsedDueDate = dueDate ? new Date(dueDate) : event.dueDate;
        if (isNaN(parsedDueDate.getTime())) {
            return res.status(400).json({ message: 'Invalid due date format.' });
        }
        const parsedReminderDate = reminderDate ? new Date(reminderDate) : event.reminderDate;
        if (parsedReminderDate && isNaN(parsedReminderDate.getTime())) {
             return res.status(400).json({ message: 'Invalid reminder date format.' });
        }


        event.title = title || event.title;
        event.description = description !== undefined ? description : event.description;
        event.type = type || event.type;
        event.dueDate = parsedDueDate;
        event.reminderDate = parsedReminderDate !== undefined ? parsedReminderDate : event.reminderDate;
        event.courseId = courseId !== undefined ? courseId : event.courseId;
        event.assignedToUserId = assignedToUserId !== undefined ? assignedToUserId : event.assignedToUserId;
        event.quizId = quizId !== undefined ? quizId : event.quizId;
        event.assignmentId = assignmentId !== undefined ? assignmentId : event.assignmentId;

        await event.save();
        res.status(200).json({ message: 'Event updated successfully.', event });
    } catch (error) {
        console.error('Error updating event:', error);
        res.status(500).json({ message: 'Error updating event', error: error.message });
    }
};

// @route DELETE /api/calendar/events/:id
// @desc Delete an event
// @access Private (requires authentication and authorization - only creator or admin)
exports.deleteEvent = async (req, res) => {
    try {
        const eventId = req.params.id;
        const userId = req.user.id;
        const userRole = req.user.role;

        const event = await Event.findByPk(eventId);
        if (!event) {
            return res.status(404).json({ message: 'Event not found.' });
        }

        // Authorization: Only the creator or an admin can delete
        if (event.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized to delete this event.' });
        }

        await event.destroy();
        res.status(200).json({ message: 'Event deleted successfully.' });
    } catch (error) {
        console.error('Error deleting event:', error);
        res.status(500).json({ message: 'Error deleting event', error: error.message });
    }
};

// @route GET /api/calendar/upcoming-reminders
// @desc Get upcoming reminders for the authenticated user (for immediate display or for background worker)
// @access Private (can be called by frontend or by internal worker)
exports.getUpcomingReminders = async (req, res) => {
    try {
        const userId = req.user.id;
        const now = new Date();
        const thirtyMinutesFromNow = new Date(now.getTime() + 30 * 60 * 1000); // Reminders within next 30 minutes

        const reminders = await Event.findAll({
            where: {
                assignedToUserId: userId, // Reminders assigned to this user
                reminderDate: {
                    [Op.between]: [now, thirtyMinutesFromNow] // Reminder date is in the near future
                },
                // Add a flag here to ensure notification is sent only once, e.g., notificationSent: false
            },
            include: [
                { model: Course, as: 'course', attributes: ['id', 'title'] },
                { model: Quiz, as: 'quiz', attributes: ['id', 'title'] }
            ],
            order: [['reminderDate', 'ASC']]
        });

        // In a real scenario, you'd likely update a 'notificationSent' flag for these events
        // after processing them to prevent sending duplicates.

        res.status(200).json(reminders);
    } catch (error) {
        console.error('Error fetching upcoming reminders:', error);
        res.status(500).json({ message: 'Error fetching upcoming reminders', error: error.message });
    }
};

// For actual server-side triggered notifications, a separate mechanism is needed.
// This function would be called by a cron job or a dedicated notification worker.
exports.checkAndSendReminders = async () => {
    try {
        const now = new Date();
        const fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000); // Look back 5 mins
        const fiveMinutesFromNow = new Date(now.getTime() + 5 * 60 * 1000); // Look forward 5 mins

        // Find events whose reminderDate falls within this small window
        // and have not yet had a notification sent (assuming a 'notificationSent' flag)
        const eventsToRemind = await Event.findAll({
            where: {
                reminderDate: {
                    [Op.between]: [fiveMinutesAgo, fiveMinutesFromNow]
                },
                // notificationSent: false // Implement this flag in your Event model
            },
            include: [
                { model: User, as: 'assignedTo', attributes: ['id', 'username'] },
                { model: Course, as: 'course', attributes: ['id', 'title'] },
                { model: Quiz, as: 'quiz', attributes: ['id', 'title'] }
            ]
        });

        for (const event of eventsToRemind) {
            if (event.assignedTo) { // Ensure there's a user to send to
                const title = `Reminder: ${event.title}`;
                const body = event.description || `Your ${event.type} is due on ${event.dueDate.toLocaleDateString()}.`;
                const data = { eventId: event.id, type: event.type, courseId: event.courseId };

                await sendNotification(event.assignedTo.id, title, body, data);

                // After sending, update the event to mark notification as sent
                // event.notificationSent = true;
                // await event.save();
            }
        }
        console.log(`Checked reminders. Sent ${eventsToRemind.length} notifications.`);
    } catch (error) {
        console.error('Error in checkAndSendReminders job:', error);
    }
};