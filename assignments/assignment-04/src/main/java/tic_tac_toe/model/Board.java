package tic_tac_toe.model;


public interface Board {

    public void updateGrid(String xOrO, int row, int col);

    public Boolean checkWinner(String xOrO);
}
