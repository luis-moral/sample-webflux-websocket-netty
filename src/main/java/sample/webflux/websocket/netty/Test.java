package sample.webflux.websocket.netty;

import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

public class Test 
{
	public static void main(String[] args)
	{
		ReplayProcessor<Integer> to = ReplayProcessor.create(5);		
		
		Flux
			.range(1, 5)
			.doOnNext(value -> System.out.println("from: " + value))
			.subscribe(value -> to.onNext(value));
		
		to
			.subscribeOn(Schedulers.elastic())
			.subscribe(value -> System.out.println("to1: " + value));
		
		to
			.subscribeOn(Schedulers.elastic())
			.subscribe(value -> System.out.println("to2: " + value));
		
		Mono
			.delay(Duration.ofMillis(500))
			.block();
		
		/**/		
		
		MonoProcessor<Integer> monoTo = MonoProcessor.create();
		
		Mono.just(1)
			.doOnNext(value -> System.out.println("monoFrom: " + value))
			.subscribe(value -> monoTo.onNext(value));
		
		monoTo
			.subscribeOn(Schedulers.elastic())
			.subscribe(value -> System.out.println("monoTo: " + value));
		
		Mono
			.delay(Duration.ofMillis(500))
			.block();
	}
}
