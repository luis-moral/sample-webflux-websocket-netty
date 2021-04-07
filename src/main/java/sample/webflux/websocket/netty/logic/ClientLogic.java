package sample.webflux.websocket.netty.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import sample.webflux.websocket.netty.handler.ClientWebSocketHandler;
import sample.webflux.websocket.netty.handler.SessionHandler;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicInteger messageId;

    public ClientLogic() {
        messageId = new AtomicInteger(0);
    }

    public Disposable start(WebSocketClient webSocketClient, URI uri, ClientWebSocketHandler clientWebSocketHandler) {
        clientWebSocketHandler
            .connected()
            .subscribe(this::doLogic);

        return
            webSocketClient
                .execute(uri, clientWebSocketHandler)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void doLogic(SessionHandler sessionHandler) {
        sessionHandler
            .connected()
            .doOnNext(session -> logger.info("Client -> connected id=[{}]", session.getId()))
            .map(session -> "Test message " + messageId.getAndIncrement())
            .subscribe(message -> {
                sessionHandler.send(message);

                logger.info("Client({}) -> sent: [{}]", sessionHandler.session().getId(), message);
            });

        sessionHandler
            .disconnected()
            .subscribe(session -> logger.info("Client({}) -> disconnected", session.getId()));

        sessionHandler
            .receive()
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(message -> logger.info("Client({}) -> received: [{}]", sessionHandler.session().getId(), message));
    }
}
