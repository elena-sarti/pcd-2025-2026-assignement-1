package tasks.view;

import tasks.controller.BoundedBufferImpl;

public class ViewImpl implements View {

	private final ViewFrame frame;
	private final ViewModelImpl viewModel;

    public ViewImpl(ViewModelImpl model, int w, int h, BoundedBufferImpl<Integer> buffer) {
        frame = new ViewFrame(model, w, h, buffer);
		frame.setVisible(true);
		this.viewModel = model;
	}
		
	@Override
    public void render() {
		frame.render();
	}
	
	@Override
    public ViewModelImpl getViewModel() {
		return viewModel;
	}
}
