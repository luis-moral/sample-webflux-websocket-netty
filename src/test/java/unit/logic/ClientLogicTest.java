package unit.logic;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import sample.webflux.websocket.netty.logic.ClientLogic;

@RunWith(MockitoJUnitRunner.class)
public class ClientLogicTest 
{
	private WebSocketClient webSocketClient;
	private URI uri;
	
	@Test
	public void testStart()
	{
		ClientLogic clientLogic = new ClientLogic();
		clientLogic.start(webSocketClient, uri);
	}

	@Before
	public void setUp()
	{
		webSocketClient = Mockito.mock(WebSocketClient.class);
		uri = Mockito.mock(URI.class);
	}
}