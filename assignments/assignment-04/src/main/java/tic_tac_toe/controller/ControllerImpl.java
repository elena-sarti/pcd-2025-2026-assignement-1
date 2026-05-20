package tic_tac_toe.controller;

import tic_tac_toe.model.BoardImpl;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ControllerImpl implements Controller {
    private final Scanner scanner;
    private BoardImpl board;
    private RemoteController adversary;

    public ControllerImpl(){
        board = new BoardImpl();
        scanner = new Scanner(System.in);
    }

    public void writeX(int r, int c){
        if (board.isValid(r, c)){
            board.updateGrid("X", r, c);
            System.out.println("The other player put X in position " + r + ", " + c);
            if (board.checkWinner("X")){
                Message msg = new Message("You won!");
                try {
                    adversary.receiveMessage(msg);
                    System.out.println("You lost!");
                } catch (RemoteException e){
                    e.printStackTrace();
                }
            } else {
                myTurn();
            }
        } else {
            Message msg = new Message("Move not valid");
            try{
                adversary.receiveMessage(msg);
            } catch (RemoteException e){
                e.printStackTrace();
            }
        }
    }

    public void writeO(int r, int c){
        if (board.isValid(r, c)){
            board.updateGrid("O", r, c);
            Message msg = new Message("The other player put O in position " + r + ", " + c);
            try {
                adversary.receiveMessage(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (board.checkWinner("O")){
                System.out.println("You won!");
                Message message = new Message("You lost!");
                try {
                    adversary.receiveMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    adversary.myTurn();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Move not valid");
        }
    }

    public void addAdversary(RemoteController adversary){
        this.adversary = adversary;
    }

    private void myTurn(){
        System.out.println("It's your turn! Insert your move.");
        System.out.println("Row: ");
        int r = scanner.nextInt();
        System.out.println("Col: ");
        int c = scanner.nextInt();
        writeO(r, c);
    }
}
