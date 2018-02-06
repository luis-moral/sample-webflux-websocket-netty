package samples.webflux.websocket.netty.handler;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class HandlerPublisher<T> implements Publisher<T>
{
	private Subscriber<? super T> subscriber = null;		

	public void subscribe(Subscriber<? super T> subscriber) 
	{
		this.subscriber = subscriber;
	}
	
	public void publish(T data)
	{
		if (subscriber != null)
		{
			subscriber.onNext(data);
		}
	}
}
