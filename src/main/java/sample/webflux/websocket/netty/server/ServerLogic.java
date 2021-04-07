package sample.webflux.websocket.netty.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sample.webflux.websocket.netty.SessionHandler;

import java.time.Duration;

public class ServerLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Disposable receiveAllSubscription;
    private Disposable receiveFirstAndSendSubscription;

    public void start(ServerHandler serverHandler, long interval) {
        serverHandler
            .connected()
            .subscribe(sessionHandler -> doLogic(sessionHandler, interval));
    }

    private void doLogic(SessionHandler sessionHandler, long interval) {
        sessionHandler
            .connected()
            .subscribe(session -> logger.info("Server -> client connected id=[{}]", session.getId()));

        sessionHandler
            .disconnected()
            .subscribe(session -> logger.info("Server -> client disconnected id=[{}]", session.getId()));

        Flux<String> receiveAll =
            sessionHandler
                .receive()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(message -> logger.info("Server -> received from client id=[{}]: [{}]", sessionHandler.session().getId(), message));

        Mono<String> receiveFirst =
            sessionHandler
                .receive()
                .subscribeOn(Schedulers.boundedElastic())
                .next();

        Flux<String> send =
            Flux
                .interval(Duration.ofMillis(interval))
                .subscribeOn(Schedulers.boundedElastic())
                .takeUntil(value -> !sessionHandler.isConnected())
                .map(value -> Long.toString(value))
                .doOnNext(sessionHandler::send)
                .doOnNext(message -> logger.info("Server -> sent: [{}] to client id=[{}]", message, sessionHandler.session().getId()));

        receiveAllSubscription = receiveAll.subscribe();
        receiveFirstAndSendSubscription = receiveFirst.thenMany(send).subscribe();
    }

    public void stop() {
        if (receiveAllSubscription != null && !receiveAllSubscription.isDisposed()) {
            receiveAllSubscription.dispose();
        }

        if (receiveFirstAndSendSubscription != null && !receiveFirstAndSendSubscription.isDisposed()) {
            receiveFirstAndSendSubscription.dispose();
        }
    }
}
