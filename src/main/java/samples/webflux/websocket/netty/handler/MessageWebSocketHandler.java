package samples.webflux.websocket.netty.handler;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MessageWebSocketHandler implements WebSocketHandler
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final ObjectMapper objectMapper;
	private final HandlerPublisher<MessageDTO> receivePublisher;
	private final Flux<MessageDTO> receiveFlux;
	private final HandlerPublisher<String> connectedPublisher;
	private final Flux<String> connectedFlux;
	
	private WebSocketSession session;
	private boolean webSocketConnected;
	
	public MessageWebSocketHandler(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
		
		receivePublisher = new HandlerPublisher<MessageDTO>();
		receiveFlux = Flux.from(receivePublisher).cache(50);
		
		connectedPublisher = new HandlerPublisher<String>();
		connectedFlux = Flux.from(connectedPublisher).cache(1);
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) 
	{
		this.session = session;		
		
		Flux<MessageDTO> receive =
			session
				.receive()
				.doOnError(t -> logger.error(t.getLocalizedMessage(), t))				
				.map(this::toMessageDTO)
				.doOnNext(receivePublisher::publish);
		
		Mono<Object> connected = 
			Mono
				.fromRunnable(() ->
				{
					webSocketConnected = true;
					connectedPublisher.publish(session.getId());					
				});				
		
		return connected.thenMany(receive).then();
	}
	
	public Flux<String> connected()
	{
		return connectedFlux;
	}
	
	public boolean isConnected()
	{
		return webSocketConnected;
	}
	
	public Flux<MessageDTO> receive()
	{
		return receiveFlux;
	}
	
	public void send(MessageDTO message)
	{		
		session
			.send(Mono.just(toWebSocketMessage(message)))
			.doOnError(ClosedChannelException.class, this::webSocketChannelClosed)
			.onErrorResume(ClosedChannelException.class, t -> Mono.empty())
			.doOnError(t -> logger.error(t.getLocalizedMessage(), t))
			.block();
	}
	
	private void webSocketChannelClosed(ClosedChannelException exception)
	{
		logger.info("WebSocket disconnected.");
		
		webSocketConnected = false;
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