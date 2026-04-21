package assignment2;

import io.vertx.core.Vertx;

public class Test_FSStat {
    public static void main(String[] args){
        String d = "C:\\Users\\Elena\\Desktop\\PCD\\pcd-2025-2026-assignement-1\\assignments\\assignment-02";
        Vertx vertx = Vertx.vertx();
        FSStat library = new FSStat(vertx);
        library
        .getFSReport(d, 30, 3)
        .onSuccess(report -> System.out.println(report.toString()));
    }
}
