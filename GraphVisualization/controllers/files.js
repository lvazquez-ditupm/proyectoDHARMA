exports.removeFile = function(req, res, next){
	const fs = require('fs');
	
	var dgram1 = require('dgram');
	var messageDHARMA = new Buffer("Finished attack: id="+req.query.id);
	var client1 = dgram1.createSocket("udp4");	

	client1.send(messageDHARMA, 0, messageDHARMA.length, 5050, "138.4.7.191", function(err) {
		client1.close();
	});
	
	res.end();
}