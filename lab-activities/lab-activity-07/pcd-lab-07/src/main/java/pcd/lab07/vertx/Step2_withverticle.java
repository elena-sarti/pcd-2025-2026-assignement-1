package pcd.lab07.vertx;

import java.io.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * 
 * Simple Verticle - i.e. an event-loop in Vert.x
 * 
 */
class MyReactiveAgent extends VerticleBase {
	//posso fare overriding di start e di stop. Nella classe identifico tutto il codice che verrà eseguito nell'event loop
	  private int cycle;
	
	  // Called when verticle is deployed
	  public Future<?> start() throws Exception {
		cycle = 0;
	
		log("1 (cycle: " + cycle + ") - doing the async call...");
		
		FileSystem fs = this.vertx.fileSystem(); // il campo vertx è l'event loop
		Future<Buffer> f1 = fs.readFile("hello.md");
		f1.onComplete((AsyncResult<Buffer> res) -> { // posso usare .onSuccess per dire cosa fare se la promise è risolta con successo
			cycle++;			
			log("4 (cycle: " + cycle + ") - hello.md \n" + res.result().toString());
		});
	
		log("2 (cycle: " + cycle + ")- doing the second async call...");

		fs
		.readFile("pom.xml")
		.onComplete((AsyncResult<Buffer> res) -> {
			cycle++;
			log("4 (cycle: " + cycle + ") - POM \n" + res.result().toString().substring(0,160));
		});
		
		log("3 (cycle: " + cycle + ") - done");
	    return super.start();
	  }

	  // Optional - called when verticle is un-deployed
	  public Future<?> stop() throws Exception {
	    return super.stop();
	  }

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step2_withverticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx
		.deployVerticle(new MyReactiveAgent()) //do a deploy verticle il mio agente asincrono. Anche deployVerticle restituisce una future, ciòè la start()
		.onSuccess(res -> {
			log("Reactive agent deployed."); //eseguita quando è eseguita la start()
		});
	}

	static private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

