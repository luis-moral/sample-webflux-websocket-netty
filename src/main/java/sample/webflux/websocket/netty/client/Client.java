package sample.webflux.websocket.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.net.URI;

public class Client {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Disposable subscription;

    public void start(WebSocketClient webSocketClient, URI uri) {
        subscription =
            webSocketClient
                .execute(uri, this::handleSession)
                .subscribe();

        logger.info("Client started.");
    }

    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }

        logger.info("Client stopped.");
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        return new ClientLogic().doLogic(session);
    }
}
