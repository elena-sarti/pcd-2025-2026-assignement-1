package tic_tac_toe.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteController extends Remote {

    public void myTurn() throws RemoteException;

    public void receiveMessage(Message msg) throws RemoteException;
}
