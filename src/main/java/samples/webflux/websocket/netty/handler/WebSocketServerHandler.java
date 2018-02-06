package samples.webflux.websocket.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class WebSocketServerHandler implements WebSocketHandler
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ObjectMapper objectMapper;
	
	private final HandlerPublisher<MessageDTO> publisher;
	private final Flux<MessageDTO> receiveFlux;
	
	private WebSocketSession session;
	
	public WebSocketServerHandler(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
		
		publisher = new HandlerPublisher<MessageDTO>();		
		receiveFlux = Flux.from(publisher).cache(50);
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		this.session = session;
		
		Flux<MessageDTO> receive =		
			session
				.receive()
				.subscribeOn(Schedulers.elastic())
				.doOnError(t -> logger.error(t.getLocalizedMessage(), t))
				.map(this::toMessageDTO)
				.doOnNext(publisher::publish);
		
		/*Mono<?> receive = 
					session
						.receive()
						.subscribeOn(Schedulers.elastic())
						.doOnNext(message -> logger.info("Received: [{}]", message.getPayloadAsText()))
						.next();*/
		
		/*Mono<Void> send =
					session.send
					(
						Flux
							.interval(Duration.ofMillis(250))
							.subscribeOn(Schedulers.elastic())
							.map(interval -> new MessageDTO(interval))
							.map(dto -> { try { return objectMapper.writeValueAsString(dto); } catch (Exception e) { throw new RuntimeException(e); } })
							.doOnNext(message -> logger.info("Sent: [{}]", message))
							.map(string -> session.textMessage(string))			
					);*/
		 
		return receive.then();
	}
	
	public Flux<MessageDTO> receive()
	{
		return receiveFlux;
	}
	
	public void send(MessageDTO message)
	{
		session
			.send(Mono.just(toWebSocketMessage(message)));
	}
	
	private MessageDTO toMessageDTO(WebSocketMessage message)
	{
		try 
		{ 
			return objectMapper.readValue(message.getPayloadAsText(), MessageDTO.class); 
		}
		catch (Exception e) 
		{ 
			throw new RuntimeException(e); 
		}
	}
	
	private WebSocketMessage toWebSocketMessage(MessageDTO message)
	{
		try 
		{ 
			return session.textMessage(objectMapper.writeValueAsString(message)); 
		} 
		catch (Exception e) 
		{ 
			throw new RuntimeException(e); 
		}
	}
}