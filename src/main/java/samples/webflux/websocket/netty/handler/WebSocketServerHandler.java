package samples.webflux.websocket.netty.handler;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class WebSocketServerHandler implements WebSocketHandler
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ObjectMapper objectMapper;
	
	public WebSocketServerHandler(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		Mono<?> receive = 
					session
						.receive()
						.subscribeOn(Schedulers.elastic())
						.doOnNext(message -> logger.info("Received: [{}]", message.getPayloadAsText()))
						.next();
		
		Mono<Void> send =
					session.send
					(
						Flux
							.interval(Duration.ofMillis(250))
							.subscribeOn(Schedulers.elastic())
							.map(interval -> new MessageDTO(interval))
							.map(dto -> { try { return objectMapper.writeValueAsString(dto); } catch (Exception e) { throw new RuntimeException(e); } })
							.doOnNext(message -> logger.info("Sent: [{}]", message))
							.map(string -> session.textMessage(string))			
					);
		 
		return receive.then(send).then();
	}
}