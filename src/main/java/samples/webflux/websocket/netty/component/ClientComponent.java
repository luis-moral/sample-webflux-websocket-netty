package samples.webflux.websocket.netty.component;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.scheduler.Schedulers;

@Component
public class ClientComponent implements ApplicationListener<ApplicationReadyEvent>
{
	@Autowired
	private WebSocketClient webSocketClient;
	
	@Autowired
	private WebSocketHandler webSocketClientHandler;
	
	@Value("${server.port}")
    private int serverPort;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
		URI uri = null;
		
		try
		{
			uri = new URI("ws://localhost:" + serverPort + "/test");
		}
		catch (URISyntaxException USe)
		{
			throw new IllegalArgumentException(USe);
		}
		
		webSocketClient
			.execute(uri, webSocketClientHandler)
			.subscribeOn(Schedulers.elastic())
			.subscribe();
	}
}
