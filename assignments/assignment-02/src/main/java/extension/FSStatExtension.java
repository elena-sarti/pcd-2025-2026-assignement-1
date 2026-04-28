package extension;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FSStatExtension {

    private volatile String lastFileFound;
    private FileSystem fs;
    private Boolean stopped = false;

    public FSStatExtension(Vertx vertx) {
        this.fs = vertx.fileSystem();
    }

    public Future<Report> getFSReport(String d, int maxFS, int nB) {
        System.out.println("Reading directory: " + d);
        return fs
                .readDir(d)
                .recover(err -> {
                    if (!stopped) {
                        System.err.println("Access error - directory: " + d + " - Cause: " + err.getMessage());
                        return Future.succeededFuture(Collections.<String>emptyList());
                    } else {
                        return Future.failedFuture("Operation stopped by user");
                    }
                })
                .compose(list -> {
                    if (!stopped) {
                        System.out.println("Directory " + d + " contains " + list.size() + " elements.");
                        List<Future<Report>> futures = new ArrayList<>();
                        for (String file : list) {
                            System.out.println("Checking: " + file);
                            futures.add(fs
                                    .props(file)
                                    .compose(props -> {
                                        if (!stopped) {
                                            if (props.isDirectory()) {
                                                return getFSReport(file, maxFS, nB);
                                            } else if (props.isRegularFile()) {
                                                setLastFileFound(file, props.size());
                                                return Future.succeededFuture(createFileReport(props.size(), maxFS, nB));
                                            }
                                            return Future.succeededFuture();
                                        } else {
                                            return Future.failedFuture("Operation stopped by user");
                                        }
                                    })
                                    .recover(err -> {
                                        if (!stopped) {
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
                                    if(!stopped) {
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

    /**
     * Creates a Report instance for a single file.
     * This helper method classifies the file into the appropriate distribution band
     * based on its size and sets the file count to one.
     *
     * @param fileSize The size of the file in KB.
     * @param maxFS    The size threshold in KB used for categorization.
     * @param numBands The total number of distribution bands.
     * @return         A new Report instance representing this single file's contribution.
     */
    private Report createFileReport(long fileSize, int maxFS, int numBands) {
        int[] fD = new int[numBands + 1];
        long sizeInKb = fileSize / 1024;
        if (sizeInKb > maxFS) {
            fD[numBands]++;
        } else {
            int index = (int) (((double) sizeInKb / maxFS) * numBands) ;
            fD[index]++;
        }
        return new Report(1, fD);
    }

    /**
     * Merges two Report objects by summing their total file counts and
     * combining their respective size distributions.
     *
     * @param r1 The first Report to merge (typically the accumulated result).
     * @param r2 The second Report to add to the first.
     * @return   A new Report instance representing the combined statistical data.
     */
    private Report mergeReports(Report r1, Report r2) {
        int totalFiles = r1.filesNumber() + r2.filesNumber();
        int[] mergedDist = new int[r1.fileSizesDistribution().length];
        for (int i = 0; i < mergedDist.length; i++) {
            mergedDist[i] = r1.fileSizesDistribution()[i] + r2.fileSizesDistribution()[i];
        }
        return new Report(totalFiles, mergedDist);
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
    }

    public String getLastFileFound() {
        return lastFileFound;
    }

    public void setLastFileFound(String file, long size){
        lastFileFound = "Found file " + file + " of size (in KB): " + String.valueOf((int) size / 1024);
    }
}
