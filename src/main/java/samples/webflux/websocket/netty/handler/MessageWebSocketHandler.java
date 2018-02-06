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

public class MessageWebSocketHandler implements WebSocketHandler
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ObjectMapper objectMapper;	
	private final HandlerPublisher<MessageDTO> receivePublisher;
	private final Flux<MessageDTO> receiveFlux;
	private final HandlerPublisher<String> connectedPublisher;
	private final Mono<String> connectedMono;
	
	private WebSocketSession session;
	
	public MessageWebSocketHandler(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
		
		receivePublisher = new HandlerPublisher<MessageDTO>();
		receiveFlux = Flux.from(receivePublisher).cache(50);
		
		connectedPublisher = new HandlerPublisher<String>();
		connectedMono = Mono.from(connectedPublisher).cache();
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		disconnect();
		
		this.session = session;
		
		Flux<MessageDTO> receive =
			session
				.receive()
				.subscribeOn(Schedulers.elastic())
				.doOnError(t -> logger.error(t.getLocalizedMessage(), t))
				.map(this::toMessageDTO)
				.doOnNext(receivePublisher::publish);
		
		connectedPublisher.complete(session.getId());
		
		return receive.then();
	}
	
	public Mono<String> connected()
	{
		return connectedMono;
	}
	
	public void disconnect()
	{
		if (session != null)
		{
			session.close();
		}
	}
	
	public Flux<MessageDTO> receive()
	{
		return receiveFlux;
	}
	
	public void send(MessageDTO message)
	{
		session.send(Mono.just(toWebSocketMessage(message))).block();
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