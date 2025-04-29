const express = require('express');
const router = express.Router();

// Add your enrollment route handlers here
router.get('/', (req, res) => {
  res.send('Enrollment route working');
});

module.exports = router;
