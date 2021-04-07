package sample.webflux.websocket.netty;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;

public class SessionHandler {

    private final Sinks.Many<String> receiveProcessor;
    private final Sinks.One<WebSocketSession> connectedProcessor;
    private final Sinks.One<WebSocketSession> disconnectedProcessor;

    private boolean webSocketConnected;
    private WebSocketSession session;

    public SessionHandler() {
        this(50);
    }

    public SessionHandler(int historySize) {
        receiveProcessor = Sinks.many().replay().all(historySize);
        connectedProcessor = Sinks.one();
        disconnectedProcessor = Sinks.one();

        webSocketConnected = false;
    }

    public Mono<Void> handle(WebSocketSession session) {
        this.session = session;

        Flux<String> receive =
            session
                .receive()
                .map(message -> message.getPayloadAsText())
                .doOnNext(receiveProcessor::tryEmitNext)
                .doOnComplete(receiveProcessor::tryEmitComplete);

        Mono<Object> connected =
            Mono
                .fromRunnable(() ->
                {
                    webSocketConnected = true;
                    connectedProcessor.tryEmitValue(session);
                });

        Mono<Object> disconnected =
            Mono
                .fromRunnable(() ->
                {
                    webSocketConnected = false;
                    disconnectedProcessor.tryEmitValue(session);
                })
                .doOnNext(value -> receiveProcessor.tryEmitComplete());

        return connected.thenMany(receive).then(disconnected).then();
    }

    public Mono<WebSocketSession> connected() {
        return connectedProcessor.asMono();
    }

    public Mono<WebSocketSession> disconnected() {
        return disconnectedProcessor.asMono();
    }

    public boolean isConnected() {
        return webSocketConnected;
    }

    public Flux<String> receive() {
        return receiveProcessor.asFlux();
    }

    public void send(String message) {
        if (isConnected()) {
            session
                .send(Mono.just(session.textMessage(message)))
                .doOnError(IOException.class, t -> connectionClosed())
                .onErrorResume(IOException.class, t -> Mono.empty())
                .subscribe();
        }
    }

    public WebSocketSession session() {
        return session;
    }

    private void connectionClosed() {
        if (isConnected()) {
            webSocketConnected = false;
            disconnectedProcessor.tryEmitValue(session);
        }
    }
}