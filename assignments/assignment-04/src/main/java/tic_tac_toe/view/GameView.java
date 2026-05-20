package tic_tac_toe.view;

import java.util.Scanner;

public class GameView {

    private final Scanner scanner;

    public GameView(){
        scanner = new Scanner(System.in);
    }

    public int[] askForMove() {
        System.out.println("It's your turn! Insert your move.");
        System.out.print("Row (0-2): ");
        int r = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Col (0-2): ");
        int c = scanner.nextInt();
        scanner.nextLine();
        return new int[]{r, c};
    }
}
