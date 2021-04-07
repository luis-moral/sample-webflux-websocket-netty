package sample.webflux.websocket.netty.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import sample.webflux.websocket.netty.server.ServerHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ServerConfiguration {

    @Value("${sample.path}")
    private String samplePath;

    @Bean
    public ServerHandler serverHandler() {
        return new ServerHandler();
    }

    @Bean
    public HandlerMapping handlerMapping(ServerHandler serverHandler) {
        Map<String, WebSocketHandler> handlerByPathMap = new HashMap<>();
        handlerByPathMap.put(samplePath, serverHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(handlerByPathMap);
        handlerMapping.setOrder(-1);

        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}