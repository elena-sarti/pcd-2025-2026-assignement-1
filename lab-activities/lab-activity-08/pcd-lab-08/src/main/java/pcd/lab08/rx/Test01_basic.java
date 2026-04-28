package pcd.lab08.rx;

import java.util.Arrays;
import io.reactivex.rxjava3.core.*;

public class Test01_basic {

	public static void main(String[] args){
				
		log("creating with just.");
		
	    Observable
	    .just("Hello world") //.just è un factory: crea un observable, con 1! elemento
	    .subscribe(s -> {	 //COMPORTAMENTO COLD: inizio a vedere gli elementi solo quando faccio subscribe. subscribe SINCRONA - fa pull, prende gli elementi nell'observable e poi fa log
	    		log(s);    	//gli el. vengono generati solo quando c'è un osservatore, con subscribe. comportamento SINCRONO
	    });
	    
	    // with inline method
	    Flowable.just("Hello world") //flowable da strutture per gestire molti dati
	    	.subscribe(System.out::println);
	    
		// creating a flow (an observable stream) from a static collection
		
	    // simple subscription 
	    
		String[] words = { "Hello", " ", "World", "!" }; 

		Flowable.fromArray(words)
			.subscribe((String s) -> {
				log(s);
			});
		
		// full subscription: onNext(), onError(), onCompleted() - per specificare come chiudere lo stream
		
		log("Full subscription...");
		
		Observable.fromArray(words)
			.subscribe((String s) -> { //onNext
				log("> " + s);
			},(Throwable t) -> { //onError
				log("error  " + t);
			},() -> { //onCompleted
				log("completed");
			});
		
		// operators

		log("simple application of operators");
		
		Flowable<Integer> flow = //stream dei quadrati degli elementi multipli di tre tra 1 e 20
		Flowable
			.range(1, 20)
			.map(v -> v * v)
			.filter(v -> v % 3 == 0);
		
		log("first subscription #1");
		flow.subscribe(System.out::println); //genero gli elementi dello stream quando faccio subscribe

		log("first subscription #2");
		flow.subscribe((v) -> {
			log("" + v);
		});

		// doOnNext for debugging...
		
		log("showing the flow...");
		
		Flowable.range(1, 20)
			.doOnNext(v -> log("1> " + v))
			.map(v -> v * v)
			.doOnNext(v -> log("2> " + v))
			.filter(v -> v % 3 == 0)
			.doOnNext(v -> log("3> " + v))
			.subscribe(System.out::println);
						
		
		// simple composition
		
		log("simple composition");
		
		Observable<String> src1 = Observable.fromIterable(Arrays.asList(
				 "the",
				 "quick",
				 "brown",
				 "fox",
				 "jumped",
				 "over",
				 "the",
				 "lazy",
				 "dog"
				));

		Observable<Integer> src2 = Observable.range(1, 5);
		
		src1
			.zipWith(src2, (string, count) -> String.format("%2d. %s", count, string))
			.subscribe(System.out::println);
		
	}
	
	private static void log(String msg) {
		System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
	}
}
