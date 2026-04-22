package assignment2;

import io.vertx.core.Vertx;

public class Test_FSStat {
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        FSStat library = new FSStat(vertx);
        String d = "C:\\Users\\Elena\\Desktop\\PCD\\pcd-2025-2026-assignement-1\\assignments\\assignment-02\\target\\classes\\assignment2";
        library
            .getFSReport(d, 2, 4)
            .onSuccess(report -> System.out.println(report.toString()));
    }
}
