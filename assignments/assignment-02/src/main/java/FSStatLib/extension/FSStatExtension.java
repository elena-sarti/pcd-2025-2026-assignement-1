package FSStatLib.extension;

import FSStatLib.Report;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FSStatExtension implements FSStatLibExtension {

    private final FileSystem fs;
    private Report lastUpdate;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public FSStatExtension(Vertx vertx) {
        this.fs = vertx.fileSystem();
    }

    @Override
    public Future<Report> getFSReport(String d, int maxFS, int nB){
        lastUpdate = new Report(0, new int[nB + 1]);
        return getReport(d, maxFS, nB);
    }

    private Future<Report> getReport(String d, int maxFS, int nB) {
        System.out.println("Reading directory: " + d);
        return fs
                .readDir(d)
                .recover(err -> {
                    if (!stopped.get()) {
                        System.err.println("Access error - directory: " + d + " - Cause: " + err.getMessage());
                        return Future.succeededFuture(Collections.<String>emptyList());
                    } else {
                        return Future.failedFuture("Operation stopped by user");
                    }
                })
                .compose(list -> {
                    if (!stopped.get()) {
                        System.out.println("Directory " + d + " contains " + list.size() + " elements.");
                        List<Future<Report>> futures = new ArrayList<>();
                        for (String file : list) {
                            System.out.println("Checking: " + file);
                            futures.add(fs
                                    .props(file)
                                    .compose(props -> {
                                        if (!stopped.get()) {
                                            if (props.isDirectory()) {
                                                return getReport(file, maxFS, nB);
                                            } else if (props.isRegularFile()) {
                                                Report localReport = createFileReport(props.size(), maxFS, nB);
                                                lastUpdate = mergeReports(localReport, lastUpdate);
                                                return Future.succeededFuture(localReport);
                                            }
                                            return Future.succeededFuture();
                                        } else {
                                            return Future.failedFuture("Operation stopped by user");
                                        }
                                    })
                                    .recover(err -> {
                                        if (!stopped.get()) {
                                            return Future.succeededFuture(new Report(0, new int[nB + 1]));
                                        } else {
                                            return Future.failedFuture("Operation stopped by user");
                                        }
                                    })
                            );
                        }
                        return Future
                                .all(futures)
                                .compose(composite -> {
                                    if (!stopped.get()) {
                                        Report total = new Report(0, new int[nB + 1]);
                                        for (int i = 0; i < composite.size(); i++) {
                                            total = mergeReports(total, composite.resultAt(i));
                                        }
                                        return Future.succeededFuture(total);
                                    } else {
                                        return Future.failedFuture("Operation stopped by user");
                                    }
                                });
                    } else {
                        return Future.failedFuture("Operation stopped by user");
                    }
                });
    }

    private Report createFileReport(long fileSize, int maxFS, int numBands) {
        int[] fD = new int[numBands + 1];
        long sizeInKb = fileSize / 1024;
        if (sizeInKb > maxFS) {
            fD[numBands]++;
        } else {
            int index = (int) (((double) sizeInKb / maxFS) * numBands);
            fD[index]++;
        }
        return new Report(1, fD);
    }

    private Report mergeReports(Report r1, Report r2) {
        int totalFiles = r1.filesNumber() + r2.filesNumber();
        int[] mergedDist = new int[r1.fileSizesDistribution().length];
        for (int i = 0; i < mergedDist.length; i++) {
            mergedDist[i] = r1.fileSizesDistribution()[i] + r2.fileSizesDistribution()[i];
        }
        return new Report(totalFiles, mergedDist);
    }

    @Override
    public Boolean getStopped() {
        return stopped.get();
    }

    @Override
    public void setStopped(Boolean stopped) {
        this.stopped.set(stopped);
    }

    @Override
    public Report getLastUpdate() {
        return lastUpdate;
    }
}
