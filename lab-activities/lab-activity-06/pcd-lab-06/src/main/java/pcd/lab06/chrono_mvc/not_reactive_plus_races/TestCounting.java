package pcd.lab06.chrono_mvc.not_reactive_plus_races;

public class TestCounting {
    /*
    voglio vedere un numero che aumenta con la possibiltà di fermarlo. M c'è un errore:
    ERRORE DI NON RESPONDIVE GUI
     */
	public static void main(String[] args) {
		var counter = new Counter(0);
		var controller = new Controller(counter);
        new CounterGUI(counter, controller).display();
	}
}
