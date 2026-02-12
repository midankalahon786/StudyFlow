const { Event, User, Course, Quiz } = require('../models');
const { Op } = require('sequelize');

// --- HELPERS ---

const parseDate = (dateStr) => {
    if (!dateStr) return null;
    const date = new Date(dateStr);
    return isNaN(date.getTime()) ? null : date;
};

const sendNotification = async (targetUserId, title, body, data = {}) => {
    console.log(`[NOTIFICATION] To User ${targetUserId}: "${title}"`, data);
};

// --- CONTROLLERS ---

exports.getEvents = async (req, res) => {
    try {
        const { startDate, endDate, courseId } = req.query;
        const userId = req.user.id;

        // Build dynamic filter
        const whereClause = {
            [Op.or]: [{ createdBy: userId }, { assignedToUserId: userId }]
        };

        if (startDate && endDate) {
            whereClause.dueDate = { [Op.between]: [new Date(startDate), new Date(endDate)] };
        }
        if (courseId) whereClause.courseId = courseId;

        const events = await Event.findAll({
            where: whereClause,
            include: [
                { model: User, as: 'creator', attributes: ['username'] },
                { model: User, as: 'assignedTo', attributes: ['username'] },
                { model: Course, as: 'course', attributes: ['title'] },
                { model: Quiz, as: 'quiz', attributes: ['title'] }
            ],
            order: [['dueDate', 'ASC']]
        });

        res.status(200).json(events);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.createEvent = async (req, res) => {
    try {
        const { title, type, dueDate, reminderDate } = req.body;
        
        if (!title || !type || !dueDate) {
            return res.status(400).json({ message: 'Missing required fields.' });
        }

        const dates = {
            dueDate: parseDate(dueDate),
            reminderDate: parseDate(reminderDate)
        };

        if (!dates.dueDate) return res.status(400).json({ message: 'Invalid date format.' });

        const event = await Event.create({
            ...req.body,
            ...dates,
            createdBy: req.user.id
        });

        res.status(201).json(event);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.getUpcomingReminders = async (req, res) => {
    try {
        const userId = req.user.id;
        const now = new Date();
        const soon = new Date(now.getTime() + 30 * 60 * 1000); 

        const reminders = await Event.findAll({
            where: {
                assignedToUserId: userId,
                reminderDate: { [Op.between]: [now, soon] }
            },
            include: [{ model: Course, as: 'course', attributes: ['title'] }]
        });
        res.status(200).json(reminders);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.updateEvent = async (req, res) => {
    try {
        const event = await Event.findByPk(req.params.id);
        
        if (!event) return res.status(404).json({ message: 'Not found.' });
        if (event.createdBy !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized.' });
        }

        // Handle dates only if provided in body
        const updates = { ...req.body };
        if (req.body.dueDate) updates.dueDate = parseDate(req.body.dueDate);
        if (req.body.reminderDate) updates.reminderDate = parseDate(req.body.reminderDate);

        await event.update(updates);
        res.status(200).json({ message: 'Updated', event });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.deleteEvent = async (req, res) => {
    try {
        const event = await Event.findByPk(req.params.id);
        if (!event) return res.status(404).json({ message: 'Not found.' });
        
        if (event.createdBy !== req.user.id && req.user.role !== 'admin') {
            return res.status(403).json({ message: 'Unauthorized.' });
        }

        await event.destroy();
        res.status(200).json({ message: 'Deleted' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.checkAndSendReminders = async () => {
    try {
        const now = Date.now();
        const events = await Event.findAll({
            where: {
                reminderDate: { [Op.between]: [new Date(now - 300000), new Date(now + 300000)] }
            },
            include: [{ model: User, as: 'assignedTo', attributes: ['id'] }]
        });

        for (const event of events) {
            if (event.assignedTo) {
                const body = event.description || `${event.type} due on ${event.dueDate.toLocaleDateString()}`;
                await sendNotification(event.assignedTo.id, `Reminder: ${event.title}`, body);
            }
        }
        console.log(`Job complete. Sent ${events.length} reminders.`);
    } catch (error) {
        console.error('Reminder job failed:', error);
    }
};
// At the end of controllers/eventController.js
module.exports = {
    getEvents: exports.getEvents,
    createEvent: exports.createEvent,
    updateEvent: exports.updateEvent,
    deleteEvent: exports.deleteEvent,
    getUpcomingReminders: exports.getUpcomingReminders, // Ensure this is defined!
    checkAndSendReminders: exports.checkAndSendReminders
};