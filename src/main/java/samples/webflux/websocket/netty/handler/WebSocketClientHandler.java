package samples.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

public class WebSocketClientHandler implements WebSocketHandler
{
	public Mono<Void> handle(WebSocketSession session) 
	{
		return Mono.empty();
	}
}
