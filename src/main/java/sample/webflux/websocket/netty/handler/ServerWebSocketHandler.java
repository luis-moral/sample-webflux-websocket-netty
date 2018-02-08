package sample.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class ServerWebSocketHandler implements WebSocketHandler
{
	private final WebSocketSessionHandler sessionHandler;	
	private final ReplayProcessor<WebSocketSessionHandler> connectedProcessor;	
	
	public ServerWebSocketHandler()
	{
		sessionHandler = new WebSocketSessionHandler();
		
		connectedProcessor = ReplayProcessor.create();
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{		
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