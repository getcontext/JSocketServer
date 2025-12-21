# JSocketServer

written in pure Java, a socket/websocket server
- it can listen, receive and send replies
- socket version is using native Java serialization to transfer binary objects between sockets
- websocket is sending casted to string binary frame
- tested under heavy traffic

Java socket server. Currently Connection plugin done for handling WebSocket connection. 
WebSocket server is able to receive, read and send handshake also read/send data from/to client.

some code snippets refactored from stackoverflow and mozilla.org

this stuff should work on Java 1.2 and higher
