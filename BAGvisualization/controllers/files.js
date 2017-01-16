exports.removeFile = function(req, res, next){
	const fs = require('fs');
	
	var dgram1 = require('dgram');
	var messageDHARMA = new Buffer("Finished attack: id="+req.query.id);
	var client1 = dgram1.createSocket("udp4");
	
	var dgram2 = require('dgram');
	var messageMW = new Buffer("DELETE " +req.query.id);
	var client2 = dgram2.createSocket("udp4");
	

	client1.send(messageDHARMA, 0, messageDHARMA.length, 5120, "192.168.10.100", function(err) {
		client1.close();
	});

	client2.send(messageMW, 0, messageMW.length, 5000, "192.168.10.100", function(err) {
		client2.close();
	});
	res.end();


}
