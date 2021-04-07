package sample.webflux.websocket.netty.server;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class ServerHandler implements WebSocketHandler {

    private final long interval;

    public ServerHandler(long interval) {
        this.interval = interval;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return new ServerLogic().doLogic(session, interval);
    }
}