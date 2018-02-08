package sample.webflux.websocket.netty.handler;

import java.nio.channels.ClosedChannelException;

import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;

public class WebSocketSessionHandler 
{
	private final ReplayProcessor<String> receiveProcessor;
	private final MonoProcessor<Object> connectedProcessor;
	private final MonoProcessor<Object> disconnectedProcessor;
	
	private boolean webSocketConnected;
	private WebSocketSession session;	
	
	public WebSocketSessionHandler()
	{
		this(50);
	}
	
	public WebSocketSessionHandler(int historySize)
	{
		receiveProcessor = ReplayProcessor.create(historySize);		
		connectedProcessor = MonoProcessor.create();
		disconnectedProcessor = MonoProcessor.create();
		
		webSocketConnected = false;
	}
	
	public Mono<Void> handle(WebSocketSession session)
	{
		this.session = session;
		
		webSocketConnected = true;
		connectedProcessor.onNext(true);
		
		Flux<String> receive =
			session
				.receive()		
				.map(Message -> Message.getPayloadAsText())
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
	
	public Flux<String> receive()
	{
		return receiveProcessor;
	}
	
	public void send(String message)
	{		
		session
			.send(Mono.just(session.textMessage(message)))
			.doOnError(ClosedChannelException.class, this::channelClosed)
			.onErrorResume(ClosedChannelException.class, t -> Mono.empty())			
			.block();
	}
	
	private void channelClosed(ClosedChannelException exception)
	{
		webSocketConnected = false;
		disconnectedProcessor.onNext(true);
	}
}