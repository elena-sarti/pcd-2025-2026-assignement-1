package FSStatLib.reactive;

import io.reactivex.rxjava3.core.Observable;

import FSStatLib.Report;

public interface FSStatLib {
    Observable<Report> getFSReport(String d, int maxFS, int nB);
}
