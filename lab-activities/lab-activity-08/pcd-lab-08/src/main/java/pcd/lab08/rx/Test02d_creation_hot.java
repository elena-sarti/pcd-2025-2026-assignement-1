package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.observables.ConnectableObservable;

public class Test02d_creation_hot {

	public static void main(String[] args) throws Exception {

		System.out.println("\n=== TEST Hot streams  ===\n"); //flussi HOT, non lazy
		
		Observable<Integer> source = Observable.create(emitter -> {		     
			new Thread(() -> {
				int i = 0;
				while (i < 200){
					try {
						log("source: "+i); 
						emitter.onNext(i);
						Thread.sleep(10);
						i++;
					} catch (Exception ex){}
				}
			}).start();
		     //emitter.setCancellable(c::close);
		 });
		
		ConnectableObservable<Integer> hotObservable = source.publish(); //per renderlo hot devo chiamare publish e connect
		hotObservable.connect();
	
		/* give time for producing some data before any subscription */
		Thread.sleep(500);
		
		log("Subscribing A.");
		
		hotObservable.subscribe((s) -> { //quando faccio subscribe, se il thread genera un nuovo elemento nello stream allora lo dice agli osservatori => perdo un po di elementi
			log("subscriber A: "+s);
			// Thread.sleep(5000);
		});	
		
		/* give time for producing some data before second subscriber */
		Thread.sleep(500);
		
		log("Subscribing B.");
		
		hotObservable.subscribe((s) -> {
			log("subscriber B: "+s); 
		});	
		
		log("Done.");
		
		Thread.sleep(10_000);

	}
	
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}
	

}
