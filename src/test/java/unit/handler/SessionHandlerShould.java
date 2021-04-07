package unit.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import sample.webflux.websocket.netty.handler.SessionHandler;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SessionHandlerShould {

    private WebSocketSession session;
    private DataBufferFactory bufferFactory;

    @BeforeEach
    public void setUp() {
        session = Mockito.mock(WebSocketSession.class);
        bufferFactory = new DefaultDataBufferFactory();
    }

    @Test public void
    receive_multiple_subscribers() {
        int values = 5;

        Mockito
            .when(session.receive())
            .thenReturn
                (
                    Flux
                        .range(1, values)
                        .map(value -> textMessage(Integer.toString(value)))
                );

        TestSessionHandler sessionHandler = new TestSessionHandler();
        Disposable connection = sessionHandler.handle(session).subscribe();

        Mono
            .delay(Duration.ofMillis(100))
            .subscribeOn(Schedulers.elastic())
            .subscribe(value -> connection.dispose());

        StepVerifier
            .create(sessionHandler.receive())
            .expectNextCount(values)
            .verifyComplete();

        StepVerifier
            .create(sessionHandler.receive())
            .expectNextCount(values)
            .verifyComplete();
    }

    @Test public void
    send_messages() {
        Mockito
            .when(session.receive())
            .thenReturn
                (
                    Flux
                        .interval(Duration.ofMillis(50))
                        .map(value -> textMessage(Long.toString(value)))
                );

        Mockito
            .when(session.send(Mockito.any()))
            .thenReturn(Mono.empty());

        Mockito
            .when(session.textMessage(Mockito.anyString()))
            .thenReturn(textMessage("Test"));

        TestSessionHandler sessionHandler = new TestSessionHandler();
        Disposable connection = sessionHandler.handle(session).subscribe();

        sessionHandler.send("Test");

        Mockito
            .verify(session, Mockito.times(1))
            .send(Mockito.any());

        Mono
            .delay(Duration.ofMillis(100))
            .subscribeOn(Schedulers.elastic())
            .subscribe(value -> connection.dispose());
    }

    private WebSocketMessage textMessage(String payload) {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = bufferFactory.wrap(bytes);
        return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
    }

    private class TestSessionHandler extends SessionHandler {

        public Mono<Void> handle(WebSocketSession session) {
            return super.handle(session);
        }
    }
}
