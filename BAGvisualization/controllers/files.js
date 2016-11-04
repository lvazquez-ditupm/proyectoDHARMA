exports.removeFile = function(req, res, next){
	const fs = require('fs');
	
	var dgram = require('dgram');
	var message = new Buffer("Finished attack: id="+req.query.id+"");
	var client = dgram.createSocket("udp4");
	client.send(message, 0, message.length, 512, "127.0.0.1", function(err) {
		console.log("Eliminar id= "+req.query.id+"");
		client.close();
	});


}