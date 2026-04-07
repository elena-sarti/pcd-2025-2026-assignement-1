package pcd.lab07.vertx;

import java.util.List;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * 
 * Chaining async calls by using Vert.x futures (based on promises)
 * 
 */
class TestChain extends VerticleBase {
	
	public Future<?> start() throws Exception {
		FileSystem fs = vertx.fileSystem();    		

		fs.readFile("hello.md") //restituisce Future<Buffer> => mi aspetto di avere un Buffer come risultato
		.compose((Buffer buf) -> { //compose(): metodo delle promise per concatenare i risultati.
            // 3 chiamate asincrone, ma sequenzialmente. faccio una chiamata solo se ho ricevuto il risultato di quella prima,
            // però non sono bloccanti
			log("1 - hello.md: \n" + buf.toString());
			return fs.readFile("pom.xml");
		}).compose((Buffer buf) -> { //per non avere nesting, con il chaining, ritorno ogni volta una promise.
			log("2 - POM: \n" + buf.toString().substring(0,160));
			return fs.readDir("src");
		}).onComplete((AsyncResult<List<String>> list) -> {
			log("3 - DIR: \n" + list.result());
		});
		
		return super.start();
	}

	private void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ " + Thread.currentThread() + " ] " + msg);
	}
}

public class Step3_chaining {

	public static void main(String[] args) {
		Vertx  vertx = Vertx.vertx();
		vertx.deployVerticle(new TestChain());
	}
}

