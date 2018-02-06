package samples.webflux.websocket.netty.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO 
{
	private final long value;
	
	@JsonCreator
	public MessageDTO(@JsonProperty("value") long value)
	{
		this.value = value;
	}
	
	public long getValue()
	{
		return value;
	}
}
