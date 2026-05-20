package tic_tac_toe;

import tic_tac_toe.controller.Controller;
import tic_tac_toe.controller.RemoteController;
import tic_tac_toe.controller.RemoteControllerImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RunSecondPlayer {

    public static void main(String[] args){

        String host = (args.length < 1) ? null : args[0];
        try {
            var registry = LocateRegistry.getRegistry(host);
            var c = (Controller) registry.lookup("Tic Tac Toe Game");

            RemoteControllerImpl localC = new RemoteControllerImpl(c);
            RemoteController localContrProxy = (RemoteController) UnicastRemoteObject.exportObject(localC, 0);

            System.out.println("Player connected to the game: the game can start! Wait for your turn.");
            c.addAdversary(localContrProxy);

        } catch (Exception e) {
            System.out.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
