package assignment2Reactive;

import java.io.IOException;

public class TestFSStat {
    public static void main(String[] args) throws IOException {
        String d = "D:\\Elena";
        FSStat lib = new FSStat();
        Report report = lib.getFSReport(d, 300, 3);
        System.out.println("Report for directory " + d + ":\n" + report.toString());
    }
}
