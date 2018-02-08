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
		
		Flux<String> receive =
			session
				.receive()
				.map(Message -> Message.getPayloadAsText())
				.doOnNext(receiveProcessor::onNext);
		
		Mono<Object> connected =
				Mono
					.fromRunnable(() -> 
					{
						connectedProcessor.onNext(true);
						webSocketConnected = true;
					});

		Mono<Object> disconnected =
				Mono
					.fromRunnable(() -> 
					{
						disconnectedProcessor.onNext(true);
						webSocketConnected = false;								
					});
			
		return connected.thenMany(receive).then(disconnected).then();
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
			disconnectedProcessor.onNext(true);
		}
	}
}