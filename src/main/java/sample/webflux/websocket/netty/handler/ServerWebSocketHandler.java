package sample.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class ServerWebSocketHandler implements WebSocketHandler
{
	private final ReplayProcessor<WebSocketSessionHandler> connectedProcessor;	
	
	public ServerWebSocketHandler()
	{
		connectedProcessor = ReplayProcessor.create();
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{		
		WebSocketSessionHandler sessionHandler = new WebSocketSessionHandler();
		
		sessionHandler
			.connected()
			.doOnNext(value -> connectedProcessor.onNext(sessionHandler))			
			.subscribe();
		
		return sessionHandler.handle(session);
	}
	
	public Flux<WebSocketSessionHandler> connected()
	{
		return connectedProcessor;
	}
}