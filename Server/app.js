/*** Creation of variables ***/
var express = require('express')
	, app = express()
	, server = require('http').createServer(app)
	, io = require('socket.io').listen(server);

var Sequelize = require("sequelize")
	, database = new Sequelize('{{DATABASE}}', '{{USERNAME}}', '{{PASSWORD}}', {
		host: "{{HOST}}",
		port: 3306
	})

/*** Creation of Database Models ***/
var Event = database.define('mb_chat_log', {
		id       : Sequelize.INTEGER,
		type     : Sequelize.STRING,
		channel  : Sequelize.STRING,
		nickname : Sequelize.STRING,
		message  : Sequelize.TEXT
	});

var UserChannel = database.define('mb_user_channel', {
		user    : Sequelize.INTEGER,
		channel : Sequelize.STRING
	});

/*** Initialization ***/
server.listen(8080);

io.set('log level', 1);

io.sockets.on('connection', function(socket)
{
	// Create a username for the client and return it
	socket.nickname   = 'Guest' + Math.floor(Math.random() * 10001);
	socket.emit('nickchanged',
	{
		nickname: socket.nickname
	});

	socket.on('channel_list', function(data)
	{
		// This event is always called first, it contains the device identifier
		socket.identifier = data.identifier;

		// Send a list of all channels the client is subscribed to
		UserChannel
			.all(
			{
				where: {
					user: socket.identifier
				},
				order: 'channel ASC'
			})
			.success(function(channels)
			{
				// Subscribe user to all channels
				for (i = 0; i < channels.length; i++)
				{
					socket.join(channels[i].channel);
				}

				// Send channels to user
				socket.emit('channel_list',
				{
					channels: channels
				});
			});
	});

	socket.on('channel_log', function(data)
	{
		// Give the client a log of the room
		Event.all(
		{
			where: {
				channel: data.channel
			},
			order: 'id DESC',
			limit: 100
		}).success(function(events)
		{
			socket.emit('log',
			{
				channel: data.channel,
				history: events
			});
		});
	});

	socket.on('join', function(data)
	{
		// Notify all connections in the room about the join
		io.sockets.in(data.channel).emit('user_joined', 
		{
			channel: data.channel,
			nickname: socket.nickname
		});

		// Connect the client to the room
		socket.join(data.channel);

		// Give the client a list of all users in the room
		var list = Array();
		var users = io.sockets.clients(data.channel);

		for (var uid in users)
		{
			list.push(users[uid].nickname);
		}

		socket.emit('user_list', 
		{
			users: list,
			channel: data.channel
		});

		// Save the event to the database
		Event
			.create(
			{
				type     : 'join',
				channel  : data.channel,
				nickname : socket.nickname,
				message  : ''
			})
			.error(function(e)
			{
				console.log("Error saving event 'join': " + e);
			})
			.success(function(e)
			{
				console.log("Saved event 'join': " + e.values);
			});

		// Save the subscription the the database
		database
			.query("INSERT INTO mb_user_channels (user, channel) VALUES ('" + socket.identifier + "', '" + data.channel + "')")
			.error(function(e)
			{
				console.log("Error saving subscription: " + e);
			})
			.success(function(e)
			{
				console.log("Saved subscription: " + e);
			});

		// Give the client a log of the room
		Event.all(
		{
			where: {
				channel: data.channel
			},
			order: 'id DESC',
			limit: 100
		}).success(function(events)
		{
			socket.emit('log',
			{
				channel: data.channel,
				history: events
			});
		});
	});

	socket.on('part', function(data)
	{
		// Notify all connections in the room about the part
		socket.leave(data.channel);
		io.sockets.in(data.channel).emit('user_parted', 
		{
			channel: data.channel,
			nickname: socket.nickname
		});

		// Save the event to the database
		Event
			.create(
			{
				type     : 'part',
				channel  : data.channel,
				nickname : socket.nickname,
				message  : ''
			})
			.error(function(e)
			{
				console.log("Error saving event 'part': " + e);
			})
			.success(function(e)
			{
				console.log("Saved event 'part': " + e.values);
			});

		// Delete subscription from the database
		database
			.query("DELETE FROM mb_user_channels WHERE user = '" + socket.identifier + "' AND channel = '" + data.channel + "'")
			.success(function(e)
			{
				console.log("Deleted subscription: " + e);
			});
	});

	socket.on('msg', function(data)
	{
		// Notify all connections in the room about a new message
		io.sockets.in(data.channel).emit('msg',
		{
			content: data.content,
			channel: data.channel,
			nickname: socket.nickname
		});

		// Save the event to the database
		Event
			.create(
			{
				type     : 'msg',
				channel  : data.channel,
				nickname : socket.nickname,
				message  : data.content
			})
			.error(function(e)
			{
				console.log("Error saving event 'msg': " + e);
			})
			.success(function(e)
			{
				console.log("Saved event 'msg': " + e.values);
			});
	});

	socket.on('nickchange', function(data)
	{
		// Notify everyone that the client changed his nickname
		for (var channel in io.sockets.manager.roomClients[socket.id])
		{
			channel = channel.replace('/', '');

			if (channel.length > 0)
			{
				io.sockets.in(channel).emit('user_nickchanged', 
				{
					channel: channel,
					old_nick: socket.nickname,
					new_nick: data.nickname
				});
			}
		}

		// Save the event to the database
		Event
			.create(
			{
				type     : 'nickchange',
				channel  : '',
				nickname : data.nickname,
				message  : socket.nickname
			})
			.error(function(e)
			{
				console.log("Error saving event 'nickchange': " + e);
			})
			.success(function(e)
			{
				console.log("Saved event 'nickchange': " + e.values);
			});

		// Save new nickname
		socket.nickname = data.nickname;
	});
});