package samples.webflux.websocket.netty.component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import samples.webflux.websocket.netty.handler.MessageDTO;
import samples.webflux.websocket.netty.handler.MessageWebSocketHandler;

@Component
public class ClientComponent implements ApplicationListener<ApplicationReadyEvent>
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ConfigurableApplicationContext applicationContext;
	
	@Autowired
	private WebSocketClient webSocketClient;
	
	@Autowired
	private MessageWebSocketHandler clientWebSocketHandler;
	
	@Value("${server.port}")
	private int serverPort;
	
	@Value("${sample.path}")
	private String samplePath;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{		
		URI uri = null;
		
		try
		{
			uri = new URI("ws://localhost:" + serverPort + samplePath);
		}
		catch (URISyntaxException USe)
		{
			throw new IllegalArgumentException(USe);
		}
		
		Disposable clientConnection = 		
			webSocketClient
				.execute(uri, clientWebSocketHandler)
				.subscribeOn(Schedulers.elastic())
				.subscribe();
		
		clientWebSocketHandler
			.connected()
			.doOnNext(id -> logger.info("Connected [{}]", id))
			.map(id -> new MessageDTO(0))
			.doOnNext(message -> clientWebSocketHandler.send(message))
			.doOnNext(message -> logger.info("Client Sent: [{}]", message.getValue()))
			.blockFirst();
				
		clientWebSocketHandler
			.receive()
			.subscribeOn(Schedulers.elastic())
			.subscribe(message -> logger.info("Client Received: [{}]", message.getValue()));		
		
		Mono
			.delay(Duration.ofSeconds(5))			
			.doOnNext(value -> clientConnection.dispose())
			.doOnNext(value -> logger.info("Disconnected."))
			.then(Mono.delay(Duration.ofMillis(500)))
			.map(value -> SpringApplication.exit(applicationContext, () -> 0))
			.subscribe(exitValue -> System.exit(exitValue));
	}
}
