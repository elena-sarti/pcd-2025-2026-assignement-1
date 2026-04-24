package assignment2VirtualThreads;

import java.util.Arrays;

public record Report(int filesNumber, int[] fileSizesDistribution){

    @Override
    public String toString() {
        return "Number of files contained in directory: " + filesNumber +
                "\nDistribution of file sizes: " + Arrays.toString(fileSizesDistribution);
    }
}