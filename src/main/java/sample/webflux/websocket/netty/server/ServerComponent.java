package sample.webflux.websocket.netty.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class ServerComponent implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ServerHandler serverHandler;

    @Value("${sample.send-interval}")
    private long sendInterval;

    private ServerLogic serverLogic;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        serverLogic = new ServerLogic();
        serverLogic.start(serverHandler, sendInterval);
    }

    @PreDestroy
    public void onExit() {
        if (serverLogic != null) {
            serverLogic.stop();
        }
    }
}