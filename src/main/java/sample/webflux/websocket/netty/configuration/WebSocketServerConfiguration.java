package sample.webflux.websocket.netty.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;

@Configuration
public class WebSocketServerConfiguration 
{
	@Value("${sample.path}")
	private String samplePath;
	
	@Bean
	public ServerWebSocketHandler serverWebSocketHandler()
	{
		return new ServerWebSocketHandler();
	}
	
	@Bean
	public HandlerMapping handlerMapping(ServerWebSocketHandler serverWebSocketHandler) 
	{
		Map<String, WebSocketHandler> handlerByPathMap = new HashMap<String, WebSocketHandler>();
		handlerByPathMap.put(samplePath, serverWebSocketHandler);

		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setUrlMap(handlerByPathMap);
		handlerMapping.setOrder(-1);

		return handlerMapping;
	}
	
	@Bean
	public WebSocketHandlerAdapter handlerAdapter() 
	{
		return new WebSocketHandlerAdapter();
	}
}