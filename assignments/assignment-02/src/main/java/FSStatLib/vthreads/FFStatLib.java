package FSStatLib.vthreads;

import FSStatLib.Report;

import java.util.concurrent.Future;

public interface FFStatLib {
    Future<Report> getFSReport(String d, int maxFS, int nB);
}
