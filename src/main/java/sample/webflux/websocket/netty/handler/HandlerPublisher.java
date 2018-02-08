package sample.webflux.websocket.netty.handler;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class HandlerPublisher<T> implements Publisher<T>
{
	private Subscriber<? super T> subscriber = null;		

	public void subscribe(Subscriber<? super T> subscriber) 
	{
		this.subscriber = subscriber;
		
		subscriber.onSubscribe(new Subscription() 
		{
			@Override
			public void request(long n) 
			{
			}
			
			@Override
			public void cancel() 
			{
			}
		});
	}
	
	public void publish(T data)
	{
		if (subscriber != null)
		{
			subscriber.onNext(data);
		}
	}
	
	public void complete()
	{
		if (subscriber != null)
		{
			subscriber.onComplete();
		}
	}
}
