package FSStatLib.reactive;

public class TestFSStat {
    public static void main(String[] args){
        String d = "D:\\Elena";
        FSStatLib lib = new FSStatLib();
        lib.getFSReport(d, 300, 3)
            .blockingSubscribe(
                    report -> System.out.println("Report for directory " + d + ":\n" + report)
            );
    }
}
