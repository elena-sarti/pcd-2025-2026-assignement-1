package assignment2EventLoop;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FSStat {

    FileSystem fs;

    public FSStat(Vertx vertx) {
        this.fs = vertx.fileSystem();
    }

    public Future<Report> getFSReport(String d, int maxFS, int nB) {
        return scanAndPopulate(d, maxFS, nB);
    }

    /**
     * Asynchronously performs a recursive traversal of the directory structure.
     * It generates a Future for each child element and aggregates the results
     * using composition once all operations are complete.
     *
     * @param path  The current directory path being processed.
     * @param maxFS The size threshold in KB for categorization.
     * @param nB    The number of distribution bands.
     * @return      A Future containing the aggregated Report for the current sub-tree.
     */
    private Future<Report> scanAndPopulate(String path, int maxFS, int nB) {
        System.out.println("Reading directory: " + path);
        return fs
                .readDir(path)
                .recover(err -> {
                    System.err.println("Access error - directory: " + path + " - Cause: " + err.getMessage());
                    return Future.succeededFuture(Collections.<String>emptyList()); // Restituisci lista vuota
                })
                .compose(list -> {
                    System.out.println("Directory " + path + " contains " + list.size() + " elements.");
                    List<Future<Report>> futures = new ArrayList<>();
                    for (String file : list) {
                        System.out.println("Sto controllando: " + file);
                        futures.add(fs
                                .props(file)
                                .compose(props -> {
                                    if (props.isDirectory()) {
                                        return scanAndPopulate(file, maxFS, nB);
                                    } else if (props.isRegularFile()) {
                                        return Future.succeededFuture(createFileReport(props.size(), maxFS, nB));
                                    }
                                    return Future.succeededFuture();
                                })
                                .recover(err ->
                                        Future.succeededFuture(new Report(0, new int[nB + 1])))
                        );
                    }
                    return Future
                            .all(futures)
                            .map(composite -> {
                                Report total = new Report(0, new int[nB + 1]);
                                for (int i = 0; i < composite.size(); i++) {
                                    total = mergeReports(total, composite.resultAt(i));
                                }
                                return total;
                            });
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
            int index = (int) (((double) sizeInKb / maxFS) * numBands + 1) ;
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

}
