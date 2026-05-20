package main.sketch02.controller;

import main.sketch02.model.Counter;

public interface Cmd {
	
	void execute(Counter c);
}
