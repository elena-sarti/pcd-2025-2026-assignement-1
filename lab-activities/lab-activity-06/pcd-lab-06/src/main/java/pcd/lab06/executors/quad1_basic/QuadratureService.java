package pcd.lab06.executors.quad1_basic;

import java.util.concurrent.*;

public class QuadratureService {

	private int numTasks;
	private int poolSize;
	private ExecutorService executor;
	
	public QuadratureService (int numTasks, int poolSize){		
		this.numTasks = numTasks;
		this.poolSize = poolSize;
	}
	
	public double compute(IFunction mf, double a, double b) throws InterruptedException { 
		executor = Executors.newFixedThreadPool(poolSize);
		QuadratureResult result = new QuadratureResult();		
		double x0 = a;
		double step = (b-a)/numTasks;		
		for (int i = 0; i < numTasks; i++) {
			try {
				executor.execute(new ComputeAreaTask(x0, x0 + step, mf, result));
				log("submitted task " + x0 + " " + (x0+step));
				x0 += step;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}				

		executor.shutdown(); //dato che non ho la garanzia che tutti hanno finito, dato che non ho le future e non chiamo get, devo chiudere l'executor
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);		
		double res = result.getResult();
		return res;
	}
	
	
	private void log(String msg){
		System.out.println("[SERVICE] "+msg);
	}
}
