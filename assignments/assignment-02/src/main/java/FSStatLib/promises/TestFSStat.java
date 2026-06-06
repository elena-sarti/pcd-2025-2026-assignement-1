package FSStatLib.promises;

import io.vertx.core.Vertx;

public class TestFSStat {

    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        FSStatLib library = new FSStatLib(vertx);
        String d = "D:\\Elena";
        library.getFSReport(d, 300, 3)
                .onSuccess(report -> System.out.println("Report for directory " + d + ":\n" + report));
    }
}
