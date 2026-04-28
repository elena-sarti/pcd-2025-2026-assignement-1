package pcd.sequential.view;

import pcd.sequential.controller.BoundedBufferImpl;

public class View {

	private ViewFrame frame;
	private ViewModel viewModel;
    private BoundedBufferImpl<Integer> buffer;
	
	public View(ViewModel model, int w, int h, BoundedBufferImpl<Integer> buffer) {
        this.buffer = buffer;
		frame = new ViewFrame(model, w, h, buffer);
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	public void render() {
		frame.render();
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}
}
