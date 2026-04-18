package pcd.lab08.rx;

import io.reactivex.rxjava3.core.*;

public class Test02b_creation_async {

	public static void main(String[] args) throws Exception {

		log("Creating an observable (cold) using its own thread.");

		Observable<Integer> source = Observable.create(emitter -> {		     
			new Thread(() -> { //creo un thread che genera 20 elementi e li mette nel flusso
				int i = 0;
				while (i < 20){
					try {
						log("source: "+i); 
						emitter.onNext(i);
						Thread.sleep(200);
						i++;
					} catch (Exception ex){}
				}
				emitter.onComplete();
			}).start();
		 });

		Thread.sleep(1000);
		
		log("Subscribing A.");
		
		source.subscribe((s) -> { //ogni volta che sottoscrivo, la lambda genera un threads nuovo
			log("Subscriber A: " + s); 
		});	

		// Thread.sleep(1000);

		log("Subscribing B.");

		source.subscribe((s) -> { //2 subscribe => 2 thread, con lo stesso comportamento
			log("Subscriber B: " + s); 
		});	

		log("Done.");
	}
	
	static private void log(String msg) {
		System.out.println("[ " + Thread.currentThread().getName() + "  ] " + msg);
	}

}
