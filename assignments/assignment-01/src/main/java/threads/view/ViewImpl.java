package threads.view;

import threads.controller.BoundedBufferImpl;

public class ViewImpl implements View {

	private ViewFrame frame;
	private ViewModelImpl viewModel;
    private BoundedBufferImpl<Integer> buffer;
	
	public ViewImpl(ViewModelImpl model, int w, int h, BoundedBufferImpl<Integer> buffer) {
        this.buffer = buffer;
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
