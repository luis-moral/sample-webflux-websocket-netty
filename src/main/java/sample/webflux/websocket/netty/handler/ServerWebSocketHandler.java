package sample.webflux.websocket.netty.handler;

import java.util.LinkedList;
import java.util.List;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class ServerWebSocketHandler implements WebSocketHandler
{
	private final ReplayProcessor<WebSocketSessionHandler> connectedProcessor;
	private final List<WebSocketSessionHandler> sessionList;
	
	public ServerWebSocketHandler()
	{
		connectedProcessor = ReplayProcessor.create();
		sessionList = new LinkedList<WebSocketSessionHandler>();
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		WebSocketSessionHandler sessionHandler = new WebSocketSessionHandler();		
		connectedProcessor.onNext(sessionHandler);
		
		sessionHandler
			.connected()
			.subscribe(value -> sessionList.add(sessionHandler));
		
		sessionHandler
			.disconnected()
			.subscribe(value -> sessionList.remove(sessionHandler));
		
		return sessionHandler.handle(session);
	}
	
	public Flux<WebSocketSessionHandler> connected()
	{
		return connectedProcessor;
	}
}