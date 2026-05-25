package tasks.view;

import tasks.controller.BoundedBufferImpl;

public class View {

	private ViewFrame frame;
	private ViewModelImpl viewModel;
    private BoundedBufferImpl<Integer> buffer;
	
	public View(ViewModelImpl model, int w, int h, BoundedBufferImpl<Integer> buffer) {
        this.buffer = buffer;
		frame = new ViewFrame(model, w, h, buffer);
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModelImpl getViewModel() {
		return viewModel;
	}
}
