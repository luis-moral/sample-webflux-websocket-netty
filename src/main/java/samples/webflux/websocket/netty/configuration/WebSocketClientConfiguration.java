package samples.webflux.websocket.netty.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import samples.webflux.websocket.netty.handler.WebSocketClientHandler;

@Configuration
public class WebSocketClientConfiguration 
{
	@Bean
	public WebSocketClient webSocketClient()
	{
		return new ReactorNettyWebSocketClient();
	}
	
	@Bean
	public WebSocketClientHandler webSocketClientHandler()
	{
		return new WebSocketClientHandler();
	}
}
