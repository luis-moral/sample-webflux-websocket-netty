package sample.webflux.websocket.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Optional;

public class Client {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Sinks.Many<String> sendBuffer;
    private Sinks.Many<String> receiveBuffer;
    private Disposable subscription;
    private WebSocketSession session;

    public void connect(WebSocketClient webSocketClient, URI uri) {
        sendBuffer = Sinks.many().unicast().onBackpressureBuffer();
        receiveBuffer = Sinks.many().unicast().onBackpressureBuffer();

        subscription =
            webSocketClient
                .execute(uri, this::handleSession)
                .then(Mono.fromRunnable(this::onClose))
                .subscribe();

        logger.info("Client connected.");
    }

    public void disconnect() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            subscription = null;

            onClose();
        }

        logger.info("Client disconnected.");
    }

    public void send(String message) {
        sendBuffer.tryEmitNext(message);
    }

    public Flux<String> receive() {
        return receiveBuffer.asFlux();
    }

    public Optional<WebSocketSession> session() {
        return Optional.ofNullable(session);
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        onOpen(session);

        Mono<Void> input =
            session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(receiveBuffer::tryEmitNext)
                .then();

        Mono<Void> output =
            session
                .send(
                    sendBuffer
                        .asFlux()
                        .map(session::textMessage)
                );

        return
            Mono
                .zip(input, output)
                .then();
    }

    private void onOpen(WebSocketSession session) {
        this.session = session;

        logger.info("Session opened");
    }

    private void onClose() {
        session = null;

        logger.info("Session closed");
    }
}
