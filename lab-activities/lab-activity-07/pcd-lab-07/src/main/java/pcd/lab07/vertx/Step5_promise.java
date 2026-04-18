package pcd.lab07.vertx;

import io.vertx.core.*;


/**
 * 
 * Using promises (i.e. the inner side of Vert.x futures) 
 * 
 */
class VerticleWithPromise extends VerticleBase {
	
	public Future<?> start() throws Exception {
		log("started.");	
		var fut = this.getDelayedRandom(1000);
		fut.onComplete((res) -> {
			System.out.println("Result: " + res.result());	
		});
		return super.start();
	}

	/**
	 * 
	 * Implementing an async method using promises.
	 * 
	 * The method returns a random value after 
	 * some specified time (delay)
	 * 
	 * @param delay
	 * @return
	 */
	protected Future<Double> getDelayedRandom(int delay){ //metodo asincrono
		Promise<Double> promise = Promise.promise(); // quando creo una promise, il tipo è promise
		this.vertx.setTimer(delay, (res) -> { // specifico la callback da chiamare quando è risolto il timer
			var num = Math.random();
			promise.complete(num); // dico che la promise è completata con .complete()
		});
		return promise.future(); //posso ottenere la future corrispondente alla promise - il lato che vede il client, subito dato al client. per agganciarci .onComplete
	}
	
	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step5_promise {
	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new VerticleWithPromise());
		
	}
}

