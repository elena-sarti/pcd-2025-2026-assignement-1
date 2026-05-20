package main.sketch02.controller;

import main.sketch02.model.Counter;

public class IncCmd implements Cmd {

	@Override
	public void execute(Counter c) {
		c.inc();
	}

}
