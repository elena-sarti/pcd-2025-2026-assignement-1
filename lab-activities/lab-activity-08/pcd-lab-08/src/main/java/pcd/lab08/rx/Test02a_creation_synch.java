package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;

public class Test02a_creation_synch {

	public static void main(String[] args){
		
	    log("Creating a observable (cold).");

	    Observable<Integer> source = Observable.create(emitter -> { //create per generare un flusso
	        for (int i = 0; i <= 2; i++) {
	            log("source: " + i);
	            emitter.onNext(i); //devo chiamare onNext per aggiungere ogni elemento
	        }
	        emitter.onComplete(); //devo chiamare onComplete alla fine
	    });

	    log("Subscribing A");
	    
	    source.subscribe(v -> log("A: "+v)); //sempre lo stesso thread che fa generazione ed osservazione

	    log("Subscribing B");
	    
	    source.subscribe(v -> log("B: "+v));

	}
	
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + " ] " + msg);
	}
}
