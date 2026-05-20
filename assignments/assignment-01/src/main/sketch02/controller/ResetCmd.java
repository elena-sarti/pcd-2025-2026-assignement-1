package main.sketch02.controller;

import main.sketch02.model.Counter;

public class ResetCmd implements Cmd {

	@Override
	public void execute(Counter c) {
		c.reset();
	}

}
