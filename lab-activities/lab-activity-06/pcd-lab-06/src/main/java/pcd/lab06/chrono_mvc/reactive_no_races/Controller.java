package pcd.lab06.chrono_mvc.reactive_no_races;

public class Controller {

	private static final int DELTA_TIME = 10;
	private Flag stopFlag;
	private CounterAgent agent;
	private Counter counter;
	
	public Controller(Counter counter) {
		this.counter = counter;
		this.stopFlag = new Flag();
	}
	
	public void notifyStarted() {
		agent = new CounterAgent(counter, stopFlag, DELTA_TIME); //non chiama metodi dell'EDT => non fa fare all'EDT cose che non gli appartengono
		agent.start();				
	}
	
	public void notifyStopped() {
		stopFlag.set();
	}

	public void notifyReset() {
		counter.reset();
		stopFlag.reset();
	}
}
