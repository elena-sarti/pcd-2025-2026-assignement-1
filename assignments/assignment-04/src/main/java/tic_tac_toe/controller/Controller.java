package tic_tac_toe.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Controller extends Remote {

    public void writeX(int row, int col) throws RemoteException;

    public void writeO(int row, int col) throws RemoteException;

    public void addAdversary(RemoteController adv) throws RemoteException;

}
