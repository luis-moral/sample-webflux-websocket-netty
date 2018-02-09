package sample.webflux.websocket.netty.handler;

import java.nio.channels.ClosedChannelException;

import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;
import reactor.ipc.netty.channel.AbortedException;

public class WebSocketSessionHandler 
{
	private final ReplayProcessor<String> receiveProcessor;
	private final MonoProcessor<WebSocketSession> connectedProcessor;
	private final MonoProcessor<WebSocketSession> disconnectedProcessor;
	
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
	
	protected Mono<Void> handle(WebSocketSession session)
	{
		this.session = session;
		
		Flux<String> receive =
			session
				.receive()
				.map(message -> message.getPayloadAsText())
				.doOnNext(textMessage -> receiveProcessor.onNext(textMessage))
				.doOnComplete(() -> receiveProcessor.onComplete());
		
		Mono<Object> connected =
				Mono
					.fromRunnable(() -> 
					{
						webSocketConnected = true;
						connectedProcessor.onNext(session);
					});

		Mono<Object> disconnected =
				Mono
					.fromRunnable(() -> 
					{
						webSocketConnected = false;
						disconnectedProcessor.onNext(session);
					})
					.doOnNext(value -> receiveProcessor.onComplete());
			
		return connected.thenMany(receive).then(disconnected).then();
	}
	
	public Mono<WebSocketSession> connected()
	{
		return connectedProcessor;
	}
	
	public Mono<WebSocketSession> disconnected()
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
		if (webSocketConnected)
		{
			session
				.send(Mono.just(session.textMessage(message)))
				.doOnError(ClosedChannelException.class, t -> connectionClosed())
				.doOnError(AbortedException.class, t -> connectionClosed())
				.onErrorResume(ClosedChannelException.class, t -> Mono.empty())
				.onErrorResume(AbortedException.class, t -> Mono.empty())
				.block();
		}	
	}
	
	private void connectionClosed()
	{
		if (webSocketConnected)
		{
			webSocketConnected = false;
			disconnectedProcessor.onNext(session);
		}
	}
}