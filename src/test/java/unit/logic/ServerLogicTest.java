package unit.logic;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;
import sample.webflux.websocket.netty.logic.ServerLogic;

@RunWith(MockitoJUnitRunner.class)
public class ServerLogicTest 
{
	private ServerWebSocketHandler serverWebSocketHandler;
	private WebSocketSessionHandler sessionHandler;
	private WebSocketSession session;
	
	@Test
	public void testStart()
	{
		MonoProcessor<WebSocketSession> connectedProcessor = MonoProcessor.create();
		MonoProcessor<WebSocketSession> disconnectedProcessor = MonoProcessor.create();
		
		Mockito
			.when(serverWebSocketHandler.connected())
			.thenReturn(Mono.just(sessionHandler).flux());
		
		Mockito
			.when(sessionHandler.connected())
			.thenReturn(connectedProcessor);
		
		Mockito
			.when(sessionHandler.disconnected())
			.thenReturn(disconnectedProcessor);
		
		Mockito
			.when(sessionHandler.receive())
			.thenReturn(Flux.fromArray(new String[] {"A", "B", "C"}).cache());
		
		ServerLogic serverLogic = new ServerLogic();
		serverLogic.start(serverWebSocketHandler, 25);
		
		connectedProcessor.onNext(session);
		disconnectedProcessor.onNext(session);
		
		Mono
			.delay(Duration.ofMillis(50))
			.block();
		
		Mockito
			.verify(sessionHandler, Mockito.times(1))
			.connected();
		
		Mockito
			.verify(sessionHandler, Mockito.times(1))
			.disconnected();
		
		Mockito
			.verify(sessionHandler, Mockito.atLeast(1))
			.receive();
		
		Mockito
			.verify(sessionHandler, Mockito.atLeast(1))
			.send(Mockito.anyString());
	}
	
	@Before
	public void setUp()
	{
		serverWebSocketHandler = Mockito.mock(ServerWebSocketHandler.class);
		sessionHandler = Mockito.mock(WebSocketSessionHandler.class);
		session = Mockito.mock(WebSocketSession.class);
	}
}
