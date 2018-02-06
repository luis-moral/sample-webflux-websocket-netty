package samples.webflux.websocket.netty.component;

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
import samples.webflux.websocket.netty.handler.MessageDTO;
import samples.webflux.websocket.netty.handler.MessageWebSocketHandler;

@Component
public class ServerComponent implements ApplicationListener<ApplicationReadyEvent>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MessageWebSocketHandler serverWebSocketHandler;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
		Flux<MessageDTO> receiveAll =
				serverWebSocketHandler
					.receive()
					.subscribeOn(Schedulers.elastic())
					.doOnNext(message -> logger.info("Received: [{}]", message.getValue()));				
		
		Mono<MessageDTO> receiveFirst =
				serverWebSocketHandler				
					.receive()
					.subscribeOn(Schedulers.elastic())
					.next();
		
		Flux<MessageDTO> send =
			Flux
				.interval(Duration.ofMillis(250))
				.subscribeOn(Schedulers.elastic())
				.map(interval -> new MessageDTO(interval))
				.doOnNext(dto -> logger.info("Sent: [{}]", dto.getValue()))
				.doOnNext(dto -> serverWebSocketHandler.send(dto));
		
		receiveAll.subscribe();
		receiveFirst.thenMany(send).subscribe();
	}
}