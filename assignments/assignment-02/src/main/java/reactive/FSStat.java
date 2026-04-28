package reactive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class FSStat {

    public Report getFSReport(String d, int maxFS, int nB) throws IOException {
        PublishSubject<File> source = PublishSubject.<File>create();
        source
            .subscribeOn(Schedulers.io())
            .subscribe();
        Observable<Report> reportObservable = source
                .map(file -> createFileReport(file.length(), maxFS, nB))
                .scan(this::mergeReports);
        new Thread(() -> {
            try {
                addFiles(source, d);
                source.onComplete();
            } catch (IOException e) {
                source.onError(e);
            }
        }).start();
        return reportObservable.blockingLast(); //blockingLast() does an implicit subscribe on reportObservable
    }

    private void addFiles(PublishSubject<File> source, String path) throws IOException {
        System.out.println("Checking directory " + path);
        Stream<Path> paths = Files.list(Paths.get(path));
        paths.forEach(p ->{
            System.out.println("Found file " + p);
            if (p.toFile().isFile()) {
                source.onNext(p.toFile());
            } else {
                try {
                    addFiles(source, p.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
