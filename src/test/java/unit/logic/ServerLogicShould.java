package unit.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import sample.webflux.websocket.netty.handler.ServerWebSocketHandler;
import sample.webflux.websocket.netty.handler.SessionHandler;
import sample.webflux.websocket.netty.logic.ServerLogic;

import java.time.Duration;

public class ServerLogicShould {

    private ServerWebSocketHandler serverWebSocketHandler;
    private SessionHandler sessionHandler;
    private WebSocketSession session;

    @BeforeEach
    public void setUp() {
        serverWebSocketHandler = Mockito.mock(ServerWebSocketHandler.class);
        sessionHandler = Mockito.mock(SessionHandler.class);
        session = Mockito.mock(WebSocketSession.class);
    }

    @Test public void
    start() {
        Sinks.One<WebSocketSession> connectedProcessor = Sinks.one();
        Sinks.One<WebSocketSession> disconnectedProcessor = Sinks.one();

        Mockito
            .when(serverWebSocketHandler.connected())
            .thenReturn(Mono.just(sessionHandler).flux());

        Mockito
            .when(sessionHandler.connected())
            .thenReturn(connectedProcessor.asMono());

        Mockito
            .when(sessionHandler.disconnected())
            .thenReturn(disconnectedProcessor.asMono());

        Mockito
            .when(sessionHandler.receive())
            .thenReturn(Flux.fromArray(new String[]{"A", "B", "C"}).cache());

        ServerLogic serverLogic = new ServerLogic();
        serverLogic.start(serverWebSocketHandler, 25);

        connectedProcessor.tryEmitValue(session);
        disconnectedProcessor.tryEmitValue(session);

        Mono
            .delay(Duration.ofMillis(50))
            .block();

        Mockito
            .verify(sessionHandler, Mockito.times(1))
            .connected();

        Mockito
            .verify(sessionHandler, Mockito.times(1))
            .disconnected();

        Mockito
            .verify(sessionHandler, Mockito.atLeast(1))
            .receive();

        Mockito
            .verify(sessionHandler, Mockito.atLeast(1))
            .send(Mockito.anyString());
    }
}
