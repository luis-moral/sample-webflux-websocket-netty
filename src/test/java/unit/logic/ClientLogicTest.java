package unit.logic;

import java.net.URI;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import sample.webflux.websocket.netty.handler.ClientWebSocketHandler;
import sample.webflux.websocket.netty.handler.WebSocketSessionHandler;
import sample.webflux.websocket.netty.logic.ClientLogic;

@RunWith(MockitoJUnitRunner.class)
public class ClientLogicTest 
{
	private WebSocketClient webSocketClient;
	private ClientWebSocketHandler clientWebSocketHandler;
	private WebSocketSessionHandler sessionHandler;
	private WebSocketSession session;
	
	@Test
	public void testStart() throws Exception
	{
		URI uri = new URI("http://127.0.0.1");
		
		MonoProcessor<WebSocketSession> connectedProcessor = MonoProcessor.create();
		MonoProcessor<WebSocketSession> disconnectedProcessor = MonoProcessor.create();
		
		Mockito
			.when(webSocketClient.execute(Mockito.eq(uri), Mockito.any()))
			.thenReturn(Mono.empty());		
		
		Mockito
			.when(clientWebSocketHandler.connected())
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
		
		ClientLogic clientLogic = new ClientLogic();
		clientLogic.start(webSocketClient, uri, clientWebSocketHandler);			
		
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
			.verify(sessionHandler, Mockito.times(1))
			.send(Mockito.anyString());		
	}

	@Before
	public void setUp()
	{
		webSocketClient = Mockito.mock(WebSocketClient.class);
		clientWebSocketHandler = Mockito.mock(ClientWebSocketHandler.class);
		sessionHandler = Mockito.mock(WebSocketSessionHandler.class);
		session = Mockito.mock(WebSocketSession.class);
	}
}