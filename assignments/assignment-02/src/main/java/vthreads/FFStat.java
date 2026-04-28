package vthreads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class FFStat {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public Future<Report> getFSReport(String d, int maxFS, int nB){
        return executor.submit(() -> {
            Report totalReport = new Report(0, new int[nB + 1]);
            System.out.println("Checking directory " + d);
            Path path = Paths.get(d);
            try (Stream<Path> stream = Files.list(path)) {
                List<Path> paths = stream.toList();
                for (Path p : paths) {
                    if (p.toFile().isDirectory()) {
                        Future<Report> reportFuture = getFSReport(p.toFile().toString(), maxFS, nB);
                        try {
                            Report localReport = reportFuture.get();
                            totalReport = mergeReports(totalReport, localReport);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Report localReport = createFileReport(p.toFile().length(), maxFS, nB);
                        totalReport = mergeReports(totalReport, localReport);
                    }
                }
            ;} catch (IOException e) {
                throw new RuntimeException(e);
            }
            return totalReport;
        });
    }

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

    private Report mergeReports(Report r1, Report r2) {
        int totalFiles = r1.filesNumber() + r2.filesNumber();
        int[] mergedDist = new int[r1.fileSizesDistribution().length];
        for (int i = 0; i < mergedDist.length; i++) {
            mergedDist[i] = r1.fileSizesDistribution()[i] + r2.fileSizesDistribution()[i];
        }
        return new Report(totalFiles, mergedDist);
    }
}
