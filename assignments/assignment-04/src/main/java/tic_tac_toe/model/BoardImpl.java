package tic_tac_toe.model;

public class BoardImpl implements Board{

    private static final int row = 3;
    private static final int cols = 3;
    private String[][] grid = new String[row][cols];

    public void updateGrid(String xOrO, int row, int col) {
        grid[row][col] = xOrO;
    }


    public Boolean checkWinner(String xOrO) {
        for (int i = 0; i < row; i++){
            if (grid[i][1].equals(xOrO) && grid[i][2].equals(xOrO) && grid[i][0].equals(xOrO)
            || grid[0][i].equals(xOrO) && grid[1][i].equals(xOrO) && grid[2][i].equals(xOrO)) {
                return true;
            }
            if (i == 1) {
                if (grid[i - 1][i - 1].equals(xOrO) && grid[i][i].equals(xOrO) && grid[i + 1][i + 1].equals(xOrO)
                || grid[i + 1][i - 1].equals(xOrO) && grid[i][i].equals(xOrO) && grid[i - 1][i + 1].equals(xOrO)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean isValid(int row, int col){
        return grid[row][col] == null;
    }

}
