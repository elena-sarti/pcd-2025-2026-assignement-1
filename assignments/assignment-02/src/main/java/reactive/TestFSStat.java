package reactive;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class TestFSStat {
    public static void main(String[] args){
        String d = "D:\\Elena";
        FSStat lib = new FSStat();
        lib.getFSReport(d, 300, 3)
            .subscribeOn(Schedulers.io())
            .blockingSubscribe(
                    report -> System.out.println("Report for directory " + d + ":\n" + report)
            );
    }
}
