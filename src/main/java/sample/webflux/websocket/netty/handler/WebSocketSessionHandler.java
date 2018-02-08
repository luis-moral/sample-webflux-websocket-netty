package sample.webflux.websocket.netty.handler;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

public class WebSocketSessionHandler 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	private final ObjectMapper objectMapper;
	private final ReplayProcessor<MessageDTO> receiveProcessor;	
	private final HandlerPublisher<String> connectedPublisher;
	private final Flux<String> connectedFlux;
	
	private WebSocketSession webSocketSession;
	private boolean webSocketConnected;
	
	public WebSocketSessionHandler(ObjectMapper objectMapper)
	{		
		this.objectMapper = objectMapper;
		
		receiveProcessor = ReplayProcessor.create(50);
		
		connectedPublisher = new HandlerPublisher<String>();
		connectedFlux = Flux.from(connectedPublisher).cache(1);		
	}
	
	public Mono<Void> handle(WebSocketSession webSocketSession)
	{
		this.webSocketSession = webSocketSession;
		
		Flux<MessageDTO> receive =
				webSocketSession
					.receive()
					.doOnError(t -> logger.error(t.getLocalizedMessage(), t))				
					.map(this::toMessageDTO)
					.doOnNext(receiveProcessor::onNext);
		
		Mono<Object> connected = 
				Mono
					.fromRunnable(() ->
					{
						webSocketConnected = true;
						connectedPublisher.publish(webSocketSession.getId());					
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
		return receiveProcessor;
	}
	
	public void send(MessageDTO message)
	{		
		webSocketSession
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
			return webSocketSession.textMessage(objectMapper.writeValueAsString(message)); 
		} 
		catch (Exception e) 
		{ 
			throw new RuntimeException(e); 
		}
	}
}
