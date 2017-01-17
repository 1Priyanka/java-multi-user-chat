# Java multi-user chat with connection encryption

To compile, run
`javac -encoding UTF8 Server.java Client.java Message.java Encryption.java`

To start the server, open the console and run one of the following:

`java Server` - the default port (1500) is used

`java Server portNumber` - the specified port is used

To start the client, open the console and run one of the following:

`java Client`

`java Client username`

`java Client username portNumber`

`java Client username portNumber serverAddress`

If the portNumber is not specified, 1500 is used

If the serverAddress is not specified "localHost", is used

If the username is not specified "Anonymous", is used

`java Client`
is equivalent to
`java Client Anonymous 1500 localhost`

To logout, type  `logout` or `LOGOUT` into the chat

To see all online users, type `who` or `WHO` into the chat