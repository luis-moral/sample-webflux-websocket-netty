package sample.webflux.websocket.netty.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class ClientWebSocketHandler implements WebSocketHandler {

    private final SessionHandler sessionHandler;
    private final Sinks.Many<SessionHandler> connectedProcessor;
    private final Sinks.Many<SessionHandler> disconnectedProcessor;

    public ClientWebSocketHandler() {
        sessionHandler = new SessionHandler();

        connectedProcessor = Sinks.many().replay().all();
        disconnectedProcessor = Sinks.many().replay().all();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessionHandler
            .connected()
            .subscribe(value -> connectedProcessor.tryEmitNext(sessionHandler));

        sessionHandler
            .disconnected()
            .subscribe(value -> disconnectedProcessor.tryEmitNext(sessionHandler));

        return sessionHandler.handle(session);
    }

    public Flux<SessionHandler> connected() {
        return connectedProcessor.asFlux();
    }

    public Flux<SessionHandler> disconnected() {
        return disconnectedProcessor.asFlux();
    }
}