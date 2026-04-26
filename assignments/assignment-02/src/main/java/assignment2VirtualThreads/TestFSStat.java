package assignment2VirtualThreads;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TestFSStat {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FFStat lib = new FFStat();
        String d = "D:\\Elena";
        Future<Report> reportFuture = lib.getFSReport(d, 300, 3);
        Report report = reportFuture.get();
        System.out.println("Report for directory " + d + ":\n" + report.toString());
    }
}
