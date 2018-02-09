package unit.logic;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;
import sample.webflux.websocket.netty.logic.ServerLogic;

@RunWith(MockitoJUnitRunner.class)
public class ServerLogicTest 
{
	private ServerWebSocketHandler serverWebSocketHandler;
	private WebSocketSessionHandler sessionHandler;
	
	@Test
	public void testStart()
	{		
		Mockito
			.when(serverWebSocketHandler.connected())
			.thenReturn(Mono.just(sessionHandler).flux());
		
		Mockito
			.when(sessionHandler.connected())
			.thenReturn(Mono.just(true));
		
		Mockito
			.when(sessionHandler.disconnected())
			.thenReturn(Mono.just(true));
		
		Mockito
			.when(sessionHandler.receive())
			.thenReturn(Flux.fromArray(new String[] {"A", "B", "C"}).cache());
		
		ServerLogic serverLogic = new ServerLogic();
		serverLogic.start(serverWebSocketHandler, 25);
		
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
	}
}
