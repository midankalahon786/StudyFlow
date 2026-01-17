const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');

router.post('/generate-quiz', aiController.generateQuizQuestions);
module.exports = router;