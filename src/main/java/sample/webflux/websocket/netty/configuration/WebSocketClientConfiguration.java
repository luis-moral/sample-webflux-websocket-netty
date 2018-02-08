package sample.webflux.websocket.netty.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import sample.webflux.websocket.netty.handler.ClientWebSocketHandler;

@Configuration
public class WebSocketClientConfiguration 
{
	@Bean
	public WebSocketClient webSocketClient()
	{
		return new ReactorNettyWebSocketClient();
	}
	
	@Bean
	public ClientWebSocketHandler clientWebSocketHandler()
	{
		return new ClientWebSocketHandler();
	}
}
