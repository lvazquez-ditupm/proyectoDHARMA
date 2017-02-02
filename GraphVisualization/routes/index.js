var express = require('express');
var router = express.Router();
var files = require('../controllers/files');

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/delete', files.removeFile);

module.exports = router;
