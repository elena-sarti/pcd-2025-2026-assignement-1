package tic_tac_toe.controller;

import java.rmi.RemoteException;
import java.util.Scanner;

public class RemoteControllerImpl implements RemoteController {
    private final Controller controller;
    private final Scanner scanner;

    public RemoteControllerImpl(Controller controller, Scanner scanner){
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void receiveMessage(Message msg) {
        if (msg.getContent().equals("Move not valid")) {
            System.out.println("Move not valid.");
            myTurn();
        } else {
            System.out.println(msg.getContent());
        }
    }

    public void myTurn(){
        System.out.println("It's your turn! Insert your move.");
        System.out.println("Row: ");
        int r = scanner.nextInt();
        System.out.println("Col: ");
        int c = scanner.nextInt();
        try{
            controller.writeX(r, c);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
