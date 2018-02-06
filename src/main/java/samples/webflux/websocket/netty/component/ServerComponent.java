package samples.webflux.websocket.netty.component;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServerComponent implements ApplicationListener<ApplicationReadyEvent>
{
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) 
	{
	}
}