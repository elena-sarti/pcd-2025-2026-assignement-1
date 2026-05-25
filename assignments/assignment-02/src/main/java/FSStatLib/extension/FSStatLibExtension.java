package FSStatLib.extension;

import FSStatLib.Report;
import io.vertx.core.Future;

public interface FSStatLibExtension {
    Future<Report> getFSReport(String d, int maxFS, int nB);

    Boolean getStopped();

    void setStopped(Boolean stopped);

    Report getLastUpdate();
}
