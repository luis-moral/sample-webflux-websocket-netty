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
		
	public Disposable start(WebSocketClient webSocketClient, URI uri, ClientWebSocketHandler clientWebSocketHandler)
	{		
		clientWebSocketHandler
			.connected()
			.subscribe(this::doLogic);
		
		Disposable clientConnection =
			webSocketClient
				.execute(uri, clientWebSocketHandler)
				.subscribeOn(Schedulers.elastic())
				.subscribe();	
		
		return clientConnection;
	}
	
	private void doLogic(WebSocketSessionHandler sessionHandler)
	{
		sessionHandler
			.connected()
			.doOnNext(value -> logger.info("Client Connected."))
			.map(value -> "Test Message")
			.doOnNext(message -> sessionHandler.send(message))
			.subscribe(message -> logger.info("Client Sent: [{}]", message));
		
		sessionHandler
			.disconnected()
			.subscribe(value -> logger.info("Client Disconnected."));
						
		sessionHandler
			.receive()
			.subscribeOn(Schedulers.elastic())
			.subscribe(message -> logger.info("Client Received: [{}]", message));
	}
}
