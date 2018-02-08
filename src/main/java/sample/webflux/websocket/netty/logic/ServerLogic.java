package sample.webflux.websocket.netty.logic;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;

public class ServerLogic 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public void start(ServerWebSocketHandler serverWebSocketHandler)
	{
		serverWebSocketHandler
			.connected()			
			.subscribe(this::doLogic);	
	}
	
	private void doLogic(WebSocketSessionHandler sessionHandler)
	{
		sessionHandler
			.connected()
			.subscribe(value -> logger.info("Server Connected [{}]", value));
		
		sessionHandler
			.disconnected()
			.subscribe(value -> logger.info("Server Disconnected [{}]", value));
		
		Flux<String> receiveAll =
			sessionHandler
				.receive()				
				.subscribeOn(Schedulers.elastic())
				.doOnNext(message -> logger.info("Server Received: [{}]", message));
		
		Mono<String> receiveFirst =
			sessionHandler
				.receive()
				.subscribeOn(Schedulers.elastic())
				.next();
		
		Flux<String> send =
			Flux
				.interval(Duration.ofMillis(500))
				.subscribeOn(Schedulers.elastic())				
				.takeUntil(value -> !sessionHandler.isConnected())
				.map(interval -> Long.toString(interval))				
				.doOnNext(message -> sessionHandler.send(message))
				.doOnNext(message -> logger.info("Server Sent: [{}]", message));
		
		receiveAll.subscribe();
		receiveFirst.thenMany(send).subscribe();
	}
}
