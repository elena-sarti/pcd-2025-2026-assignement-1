package assignment2;

import io.vertx.core.Vertx;

public class Test_FSStat {
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        FSStat library = new FSStat(vertx);
        String d = "D:\\Immagini\\Screenshot";
        library
            .getFSReport(d, 30, 3)
            .onSuccess(report -> System.out.println(report.toString()));
    }
}
