package tic_tac_toe.controller;
import tic_tac_toe.view.GameView;

import java.rmi.RemoteException;

public class RemoteControllerImpl implements RemoteController {
    private final Controller controller;
    private final GameView view;

    public RemoteControllerImpl(Controller controller){
        this.controller = controller;
        view = new GameView();
    }

    public void receiveMessage(Message msg) {
        System.out.println(msg.getContent());
        if (msg.getContent().equals("Move not valid.")) {
            myTurn();
        }
    }

    public void myTurn(){
        int[] coords = view.askForMove();
        try {
            controller.writeX(coords[0], coords[1]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
