const { Event, User, Course, Quiz } = require('../models'); 
const { Op } = require('sequelize'); // For date range queries

const sendNotification = async (targetUserId, title, body, data = {}) => {
    console.log(`[NOTIFICATION_SIMULATED] Sending to User ${targetUserId}: "${title}" - "${body}"`, data);
};

exports.getEvents = async (req, res) => {
    try {
        const userId = req.user.id; 
        const { startDate, endDate, courseId } = req.query;

        let whereClause = {
            [Op.or]: [ 
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

exports.createEvent = async (req, res) => {
    try {
        const { title, description, type, dueDate, reminderDate, courseId, assignedToUserId, quizId, assignmentId } = req.body;
        const createdBy = req.user.id; 

        if (!title || !type || !dueDate) {
            return res.status(400).json({ message: 'Title, type, and due date are required for an event.' });
        }

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
        res.status(201).json(newEvent);
    } catch (error) {
        console.error('Error creating event:', error);
        res.status(500).json({ message: 'Error creating event', error: error.message });
    }
};

exports.updateEvent = async (req, res) => {
    try {
        const eventId = req.params.id;
        const userId = req.user.id;
        const userRole = req.user.role; 

        const { title, description, type, dueDate, reminderDate, courseId, assignedToUserId, quizId, assignmentId } = req.body;

        const event = await Event.findByPk(eventId);
        if (!event) {
            return res.status(404).json({ message: 'Event not found.' });
        }
        if (event.createdBy !== userId && userRole !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized to update this event.' });
        }

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

exports.deleteEvent = async (req, res) => {
    try {
        const eventId = req.params.id;
        const userId = req.user.id;
        const userRole = req.user.role;

        const event = await Event.findByPk(eventId);
        if (!event) {
            return res.status(404).json({ message: 'Event not found.' });
        }
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

exports.getUpcomingReminders = async (req, res) => {
    try {
        const userId = req.user.id;
        const now = new Date();
        const thirtyMinutesFromNow = new Date(now.getTime() + 30 * 60 * 1000); 
        const reminders = await Event.findAll({
            where: {
                assignedToUserId: userId, 
                reminderDate: {
                    [Op.between]: [now, thirtyMinutesFromNow] 
                },  
            },
            include: [
                { model: Course, as: 'course', attributes: ['id', 'title'] },
                { model: Quiz, as: 'quiz', attributes: ['id', 'title'] }
            ],
            order: [['reminderDate', 'ASC']]
        });

        res.status(200).json(reminders);
    } catch (error) {
        console.error('Error fetching upcoming reminders:', error);
        res.status(500).json({ message: 'Error fetching upcoming reminders', error: error.message });
    }
};

exports.checkAndSendReminders = async () => {
    try {
        const now = new Date();
        const fiveMinutesAgo = new Date(now.getTime() - 5 * 60 * 1000); 
        const fiveMinutesFromNow = new Date(now.getTime() + 5 * 60 * 1000); 
        const eventsToRemind = await Event.findAll({
            where: {
                reminderDate: {
                    [Op.between]: [fiveMinutesAgo, fiveMinutesFromNow]
                },
            },
            include: [
                { model: User, as: 'assignedTo', attributes: ['id', 'username'] },
                { model: Course, as: 'course', attributes: ['id', 'title'] },
                { model: Quiz, as: 'quiz', attributes: ['id', 'title'] }
            ]
        });

        for (const event of eventsToRemind) {
            if (event.assignedTo) { 
                const title = `Reminder: ${event.title}`;
                const body = event.description || `Your ${event.type} is due on ${event.dueDate.toLocaleDateString()}.`;
                const data = { eventId: event.id, type: event.type, courseId: event.courseId };
                await sendNotification(event.assignedTo.id, title, body, data);
            }
        }
        console.log(`Checked reminders. Sent ${eventsToRemind.length} notifications.`);
    } catch (error) {
        console.error('Error in checkAndSendReminders job:', error);
    }
};