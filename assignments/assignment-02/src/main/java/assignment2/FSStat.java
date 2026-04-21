package assignment2;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;

import java.util.ArrayList;
import java.util.List;

public class FSStat {

    private final Vertx vertx;

    public FSStat(Vertx vertx){
        this.vertx = vertx;
    }
    
    public Future<Report> getFSReport(String d, int maxFS, int nB){
        FileSystem fs = vertx.fileSystem();
        return scan(fs, d)
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

    /**
     * Asynchronously scans a directory and all its subdirectories to collect file details.
     *
     * @param fs   The Vert.x FileSystem instance.
     * @param path The path of the directory to start scanning.
     * @return A Future containing a list of properties for all files found in the tree.
     */
     public Future<List<FileProps>> scan(FileSystem fs, String path){
        System.out.println("Scansionando: " + path);
        return fs
                .readDir(path)
                .compose(list -> {
                    List<Future<List<FileProps>>> promises = new ArrayList<>();
                    list.forEach(file -> {
                        String fullPath = path + "/" + file;
                        Future<FileProps> promise = fs.props(fullPath);
                        Future<List<FileProps>> combinedPromise = promise.compose(fileProps -> {
                            if (fileProps.isSymbolicLink() || fileProps.isOther()) {
                                return Future.succeededFuture(List.of()); // Ignora i link
                            }
                            if (fileProps.isDirectory()) {
                                 return scan(fs, fullPath)
                                         .map(subList -> {
                                             List<FileProps> result = new ArrayList<>();
                                             result.add(fileProps);
                                             result.addAll(subList);
                                             return result;
                                         });
                            } else {
                                return Future.succeededFuture(List.of(fileProps));
                            }
                        });
                        promises.add(combinedPromise);
                    });
                    return Future.all(promises).map(prom -> {
                        List<FileProps> result = new ArrayList<>();
                        for (int i = 0; i < prom.size(); i++) {
                            result.addAll(prom.resultAt(i));
                        }
                        return result;
                    });
                });
    }
}
