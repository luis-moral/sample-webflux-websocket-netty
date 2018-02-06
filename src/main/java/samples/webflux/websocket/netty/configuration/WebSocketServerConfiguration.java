package samples.webflux.websocket.netty.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import samples.webflux.websocket.netty.handler.WebSocketServerHandler;

@Configuration
public class WebSocketServerConfiguration 
{
	@Bean
	public WebSocketServerHandler webSocketServerHandler(ObjectMapper objectMapper)
	{
		return new WebSocketServerHandler(objectMapper);
	}
	
	@Bean
    public HandlerMapping handlerMapping(WebSocketServerHandler webSocketServerHandler) 
	{
        Map<String, WebSocketHandler> handlerByPathMap = new HashMap<String, WebSocketHandler>();
        handlerByPathMap.put("/test", webSocketServerHandler);

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
