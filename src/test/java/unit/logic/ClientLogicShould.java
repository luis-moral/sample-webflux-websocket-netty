package unit.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import sample.webflux.websocket.netty.handler.ClientWebSocketHandler;
import sample.webflux.websocket.netty.handler.SessionHandler;
import sample.webflux.websocket.netty.logic.ClientLogic;

import java.net.URI;
import java.time.Duration;

public class ClientLogicShould {

    private WebSocketClient webSocketClient;
    private ClientWebSocketHandler clientWebSocketHandler;
    private SessionHandler sessionHandler;
    private WebSocketSession session;

    @BeforeEach
    public void setUp() {
        webSocketClient = Mockito.mock(WebSocketClient.class);
        clientWebSocketHandler = Mockito.mock(ClientWebSocketHandler.class);
        sessionHandler = Mockito.mock(SessionHandler.class);
        session = Mockito.mock(WebSocketSession.class);
    }

    @Test public void
    start() throws Exception {
        URI uri = new URI("http://127.0.0.1");

        Sinks.One<WebSocketSession> connectedProcessor = Sinks.one();
        Sinks.One<WebSocketSession> disconnectedProcessor = Sinks.one();

        Mockito
            .when(webSocketClient.execute(Mockito.eq(uri), Mockito.any()))
            .thenReturn(Mono.empty());

        Mockito
            .when(clientWebSocketHandler.connected())
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

        ClientLogic clientLogic = new ClientLogic();
        clientLogic.start(webSocketClient, uri, clientWebSocketHandler);

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
            .verify(sessionHandler, Mockito.times(1))
            .send(Mockito.anyString());
    }
}