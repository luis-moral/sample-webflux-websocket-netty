package sample.webflux.websocket.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static AtomicInteger MESSAGE_ID;

    static {
        MESSAGE_ID = new AtomicInteger(0);
    }

    public Mono<Void> doLogic(WebSocketSession session) {
        return
            sendMessage(session)
                .thenMany(receiveAll(session))
                .then();
    }

    private Mono<Void> sendMessage(WebSocketSession session) {
        String message = "Test message " + MESSAGE_ID.getAndIncrement();

        return
            Mono
                .fromRunnable(() -> logger.info("Client -> connected id=[{}]", session.getId()))
                .then(
                    session
                        .send(Mono.fromCallable(() -> session.textMessage(message)))
                )
                .then(
                    Mono
                        .fromRunnable(() -> logger.info("Client({}) -> sent: [{}]", session.getId(), message))
                );
    }

    private Flux<WebSocketMessage> receiveAll(WebSocketSession session) {
        return
            session
                .receive()
                .doOnNext(message -> logger.info("Client({}) -> received: [{}]", session.getId(), message.getPayloadAsText()));
    }
}
