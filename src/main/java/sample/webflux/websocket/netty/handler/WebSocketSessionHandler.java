package sample.webflux.websocket.netty.handler;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;

public class WebSocketSessionHandler 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	private final ObjectMapper objectMapper;
	private final ReplayProcessor<MessageDTO> receiveProcessor;
	private final MonoProcessor<Object> connectedProcessor;
	private final MonoProcessor<Object> disconnectedProcessor;
	
	private WebSocketSession session;
	private boolean webSocketConnected;
	
	public WebSocketSessionHandler(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
		
		receiveProcessor = ReplayProcessor.create(50);
		connectedProcessor = MonoProcessor.create();
		disconnectedProcessor = MonoProcessor.create();
	}
	
	public Mono<Void> handle(WebSocketSession session)
	{
		this.session = session;
		
		webSocketConnected = true;
		connectedProcessor.onNext(2);
		
		Flux<MessageDTO> receive =
			session
				.receive()
				.doOnError(t -> logger.error(t.getLocalizedMessage(), t))				
				.map(this::toMessageDTO)
				.doOnNext(receiveProcessor::onNext);		
			
		return receive.then();
	}
	
	public Mono<Object> connected()
	{
		return connectedProcessor;
	}
	
	public Mono<Object> disconnected()
	{
		return disconnectedProcessor;
	}
	
	public boolean isConnected()
	{
		return webSocketConnected;
	}
	
	public Flux<MessageDTO> receive()
	{
		return receiveProcessor;
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
		disconnectedProcessor.onNext(null);
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
