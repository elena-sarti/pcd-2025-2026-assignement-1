package tic_tac_toe;

import tic_tac_toe.controller.Controller;
import tic_tac_toe.controller.ControllerImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RunFirstPlayer {

    private static final String GAME_NAME = "Tic Tac Toe Game";

    public static void main(String[] args){
        try {
            ControllerImpl controller = new ControllerImpl();
            Controller contrProxy = (Controller) UnicastRemoteObject.exportObject(controller, 0);

            var registry = LocateRegistry.getRegistry();
            registry.rebind(GAME_NAME, contrProxy);

            System.out.println("Game controller registered.");

        } catch (Exception e) {
            System.out.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
