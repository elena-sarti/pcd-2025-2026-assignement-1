package pcd.sketch03.view;

public class Renderer extends Thread{
    private View view;

    public Renderer(View view){
        this.view = view;
    }

    public void run(){
        while(true){
            view.render();
            waitAbit();
        }
    }

    private void waitAbit(){
        try {
            Thread.sleep(20);
        } catch (Exception ex) {}
    }
}
