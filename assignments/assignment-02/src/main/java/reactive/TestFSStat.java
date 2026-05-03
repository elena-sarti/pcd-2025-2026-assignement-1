package reactive;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class TestFSStat {
    public static void main(String[] args) throws InterruptedException {
        String d = "D:\\Elena";
        FSStat lib = new FSStat();
        lib.getFSReport(d, 300, 3)
            .subscribeOn(Schedulers.io())
            .subscribe(
                    report -> System.out.println("Report for directory " + d + ":\n" + report)
            );
        Thread.sleep(10000);
    }
}
