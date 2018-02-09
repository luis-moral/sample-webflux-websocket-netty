package sample.webflux.websocket.netty.handler;

import java.util.LinkedList;
import java.util.List;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServerWebSocketHandler implements WebSocketHandler
{
	private final DirectProcessor<WebSocketSessionHandler> connectedProcessor;
	private final List<WebSocketSessionHandler> sessionList;
	
	public ServerWebSocketHandler()
	{
		connectedProcessor = DirectProcessor.create();
		sessionList = new LinkedList<WebSocketSessionHandler>();
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		WebSocketSessionHandler sessionHandler = new WebSocketSessionHandler();
		
		sessionHandler
			.connected()
			.subscribe(value -> sessionList.add(sessionHandler));
		
		sessionHandler
			.disconnected()
			.subscribe(value -> sessionList.remove(sessionHandler));
		
		connectedProcessor.sink().next(sessionHandler);
		
		return sessionHandler.handle(session);
	}
	
	public Flux<WebSocketSessionHandler> connected()
	{
		return connectedProcessor;
	}
}