package FSStatLib.reactive;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import io.reactivex.rxjava3.core.*;

import FSStatLib.Report;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FSStatLib implements FSStat {

    @Override
    public Observable<Report> getFSReport(String d, int maxFS, int nB){
        return scanDirectory(Paths.get(d))
                .map(file -> createFileReport(file.length(), maxFS, nB))
                .scan(this::mergeReports)
                .lastElement()
                .toObservable();
    }

    private Observable<File> scanDirectory(Path path) {
        System.out.println("Reading directory: " + path);
        //creating a cold observable with .create()
        return Observable.<Path>create(emitter -> {
                    try (Stream<Path> paths = Files.list(path)) {
                        paths.forEach(emitter::onNext);
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .flatMap(p -> {
                    System.out.println("Checking: " + p);
                    if (p.toFile().isDirectory()) {
                        return scanDirectory(p);
                    } else {
                        return Observable.just(p.toFile());
                    }
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
