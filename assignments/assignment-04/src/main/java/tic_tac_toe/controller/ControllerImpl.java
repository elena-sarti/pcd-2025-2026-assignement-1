package tic_tac_toe.controller;

import tic_tac_toe.model.BoardImpl;
import tic_tac_toe.view.GameView;

import java.rmi.RemoteException;

public class ControllerImpl implements Controller {
    private static final String MY_SYMBOL = "O";
    private static final String OPPONENT_SYMBOL = "X";

    private BoardImpl board;
    private GameView view;
    private RemoteController adversary;

    public ControllerImpl(){
        board = new BoardImpl();
        view = new GameView();
    }

    public void addAdversary(RemoteController adversary){
        this.adversary = adversary;
        System.out.println("Player connected: the game starts!");
        myTurn();
    }

    public void writeO(int r, int c) {
        executeMove(r, c, MY_SYMBOL);
    }

    public void writeX(int r, int c) {
        executeMove(r, c, OPPONENT_SYMBOL);
    }

    private void executeMove(int r, int c, String symbol) {
        if (!board.isValid(r, c)) {
            handleInvalidMove(symbol);
            return;
        }
        board.updateGrid(symbol, r, c);
        if (symbol.equals(MY_SYMBOL)) {
            sendRemoteMessage("The other player put " + symbol + " in position " + r + "," + c);
        } else {
            System.out.println("The other player put " + symbol + " in position " + r + "," + c);
        }
        if (board.checkWinner(symbol)) {
            handleEndGame(symbol.equals(MY_SYMBOL) ? "Game over: you won!" : " Game over: you lost!");
        } else if (board.isFull()) {
            handleEndGame("Game over: even!");
        } else {
            switchTurn(symbol);
        }
    }

    private void switchTurn(String currentSymbol){
        if (currentSymbol.equals(MY_SYMBOL)) {
            System.out.println("It's now the other player's turn!");
            try {
                adversary.myTurn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            sendRemoteMessage("It's now the other player's turn!");
            myTurn();
        }
    }

    private void myTurn(){
        int[] coords = view.askForMove();
        writeO(coords[0], coords[1]);
    }

    private void handleInvalidMove(String symbol){
        if (symbol.equals(MY_SYMBOL)) {
            System.out.println("Move not valid.");
            myTurn();
        } else {
            sendRemoteMessage("Move not valid.");
        }
    }

    private void sendRemoteMessage(String text){
        try {
            adversary.receiveMessage(new Message(text));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handleEndGame(String localMessage){
        System.out.println(localMessage);
        String remoteMessage = localMessage.contains("won") ? "Game over: you lost!" :
                (localMessage.contains("lost") ? "Game over: you won!" : localMessage);
        sendRemoteMessage(remoteMessage);
    }
}
