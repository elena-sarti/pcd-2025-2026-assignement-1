package assignment2Promise;

import io.vertx.core.Vertx;

public class TestFSStat {
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        FSStat library = new FSStat(vertx);
        String d = "D:\\Elena";
        library
            .getFSReport(d, 300, 3)
            .onSuccess(report -> System.out.println(report.toString()));
    }
}
