package sample.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ClientWebSocketHandler implements WebSocketHandler
{
	private final WebSocketSessionHandler webSocketSessionHandler;
	
	private final HandlerPublisher<WebSocketSessionHandler> connectedPublisher;
	private final Flux<WebSocketSessionHandler> connectedFlux;
	
	public ClientWebSocketHandler(ObjectMapper objectMapper)
	{	
		webSocketSessionHandler = new WebSocketSessionHandler(objectMapper);
		
		connectedPublisher = new HandlerPublisher<WebSocketSessionHandler>();
		connectedFlux = Flux.from(connectedPublisher).cache(1);	
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{		
		webSocketSessionHandler
			.connected()
			.doOnNext(handler -> connectedPublisher.publish(webSocketSessionHandler))
			.subscribe();
		
		return webSocketSessionHandler.handle(session);
	}
	
	public Flux<WebSocketSessionHandler> connected()
	{
		return connectedFlux;
	}
}