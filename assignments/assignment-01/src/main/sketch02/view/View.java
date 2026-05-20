package main.sketch02.view;

import javax.swing.SwingUtilities;

import main.sketch02.controller.ActiveController;
import main.sketch02.model.Counter;
import main.sketch02.model.CounterObserver;

public class View implements CounterObserver {

	private ViewModel viewModel;
	
	private ViewFrame frame;
	
	public View(ViewModel viewModel, ActiveController controller) {
		this.viewModel = viewModel;
		frame = new ViewFrame(viewModel, controller);	
	}

	public void display() {
		SwingUtilities.invokeLater(() -> {
			frame.setVisible(true);
		});
	}
	
	@Override
	public synchronized void modelUpdated(Counter model) {
		viewModel.update(model.getCount()); // pericoloso: può causare deadlock - questa chiamata viene fatta da dentro un monitor
		frame.refresh(); //ogni volta che è aggiornato il contatore è aggiornato il modello della view poi chiamato il refresh sul frame
	}
}
