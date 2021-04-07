package sample.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.LinkedList;
import java.util.List;

public class ServerWebSocketHandler implements WebSocketHandler {

    private final Sinks.Many<SessionHandler> connectedProcessor;
    private final List<SessionHandler> sessionList;

    public ServerWebSocketHandler() {
        connectedProcessor = Sinks.many().multicast().directBestEffort();
        sessionList = new LinkedList<>();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        SessionHandler sessionHandler = new SessionHandler();

        sessionHandler
            .connected()
            .subscribe(value -> sessionList.add(sessionHandler));

        sessionHandler
            .disconnected()
            .subscribe(value -> sessionList.remove(sessionHandler));

        connectedProcessor.tryEmitNext(sessionHandler);

        return sessionHandler.handle(session);
    }

    public Flux<SessionHandler> connected() {
        return connectedProcessor.asFlux();
    }
}