package sample.webflux.websocket.netty.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.logic.ServerLogic;

@Component
public class ServerComponent implements ApplicationListener<ApplicationReadyEvent>
{	
	@Autowired
	private ServerWebSocketHandler serverWebSocketHandler;
	
	@Value("${sample.send-interval}")
	private long sendInterval;
	
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
		new ServerLogic().start(serverWebSocketHandler, sendInterval);
	}
}