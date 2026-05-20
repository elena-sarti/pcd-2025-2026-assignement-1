package main.sketch02;

import main.sketch02.controller.*;
import main.sketch02.model.AutonomousUpdater;
import main.sketch02.model.Counter;
import main.sketch02.view.View;
import main.sketch02.view.ViewModel;

public class Sketch02 {

	
	public static void main(String[] argv) {

		var model = new Counter(0);
		
		var controller = new ActiveController(model);				
		
		var viewModel = new ViewModel(model.getCount());
		var view = new View(viewModel, controller);
		model.addObserver(view);
		
		controller.start();
		
		var updater = new AutonomousUpdater(model);
		updater.start();		
		
		view.display();

	}
	
}
