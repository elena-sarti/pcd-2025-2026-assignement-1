package assignment2;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;

import java.util.List;

public class FSStat{

    private final Vertx vertx;

    public FSStat(Vertx vertx){
        this.vertx = vertx;
    }
    
    public Future<Report> getFSReport(String d, int maxFS, int nB) {
        FileSystem fs = vertx.fileSystem();
        return fs
                .readDir(d)
                .compose(list -> scan(fs, d, list))
                .map(list -> {
                    int fN = list.size();
                    int[] fD = new int[nB + 1];
                    list.forEach( e -> {
                                long sizeInKb = e.size() / 1024;
                                if (sizeInKb > maxFS) {
                                    fD[nB]++;
                                } else {
                                    int index = (int) (((double) sizeInKb / maxFS) * nB);
                                    fD[index >= nB ? nB - 1 : index]++;
                                }
                            }
                    );
                    return new Report(fN, fD);
                });
    }

    public Future<List<FileProps>> scan(FileSystem fs, String path, List<String> fileList){
        //ritornerà la future contenente tutti i props di tutti i file contenuti in d
    }
}
