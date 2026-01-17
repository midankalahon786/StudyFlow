const express = require('express');
const router = express.Router();
const db = require('../models'); 
const Notification = db.Notification; 

router.get('/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
        const notifications = await Notification.findAll({
            where: { userId: userId },
            order: [['createdAt', 'DESC']]
        });
        res.status(200).json(notifications);
    } catch (err) {
        console.error('Error fetching notifications:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

router.put('/:notificationId/read', async (req, res) => {
    const { notificationId } = req.params;
    try {
        const [updatedRows] = await Notification.update(
            { isRead: true },
            {
                where: { id: notificationId },
                returning: true, 
            }
        );
        if (updatedRows === 0) {
            return res.status(404).json({ message: 'Notification not found' });
        }
        const updatedNotification = await Notification.findByPk(notificationId);
        res.status(200).json(updatedNotification);
    } catch (err) {
        console.error('Error marking notification as read:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

router.delete('/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
        await Notification.destroy({
            where: { userId: userId }
        });
        res.status(200).json({ message: 'All notifications cleared successfully' });
    } catch (err) {
        console.error('Error clearing notifications:', err);
        res.status(500).json({ message: 'Internal Server Error' });
    }
});

module.exports = router;