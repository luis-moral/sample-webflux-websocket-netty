package sample.webflux.websocket.netty.logic;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import sample.webflux.websocket.netty.handler.ClientWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;

public class ClientLogic 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Disposable clientConnection;
		
	public void start(WebSocketClient webSocketClient, ClientWebSocketHandler clientWebSocketHandler, URI uri)
	{
		clientConnection =
			webSocketClient
				.execute(uri, clientWebSocketHandler)
				.subscribeOn(Schedulers.elastic())
				.subscribe();
		
		WebSocketSessionHandler sessionHandler = 
			clientWebSocketHandler
				.connected()
				.doOnNext(value -> logger.info("Connected [{}]", value))
				.blockFirst();
		
		sessionHandler
			.disconnected()
			.subscribe(value -> logger.info("Disconnected [{}]", value));
		
		String sendMessage = "Test Message";
		
		sessionHandler.send(sendMessage);			
		logger.info("Client Sent: [{}]", sendMessage);
				
		sessionHandler
			.receive()
			.subscribeOn(Schedulers.elastic())
			.subscribe(message -> logger.info("Client Received: [{}]", message));		
	}
	
	public void stop()
	{
		clientConnection.dispose();
		
		logger.info("Disconnected.");			
	}
}
