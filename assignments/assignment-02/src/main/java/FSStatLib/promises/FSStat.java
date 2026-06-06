package FSStatLib.promises;

import io.vertx.core.Future;

import FSStatLib.Report;

public interface FSStat {

    Future<Report> getFSReport(String d, int maxFS, int nB);
}
