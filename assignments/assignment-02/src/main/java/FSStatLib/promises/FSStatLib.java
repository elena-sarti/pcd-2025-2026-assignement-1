package FSStatLib.promises;

import FSStatLib.Report;
import io.vertx.core.Future;

public interface FSStatLib {
    Future<Report> getFSReport(String d, int maxFS, int nB);
}
