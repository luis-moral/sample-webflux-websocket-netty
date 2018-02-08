package sample.webflux.websocket.netty.component;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;

@Component
public class ServerComponent implements ApplicationListener<ApplicationReadyEvent>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ServerWebSocketHandler serverWebSocketHandler;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
		WebSocketSessionHandler sessionHandler = 
			serverWebSocketHandler
				.connected()
				.doOnNext(value -> logger.info("Client Connected [{}]", value))
				.blockFirst();
		
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