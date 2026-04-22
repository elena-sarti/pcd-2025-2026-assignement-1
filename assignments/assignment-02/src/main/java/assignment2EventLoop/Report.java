package assignment2;

import java.util.Arrays;

public record Report(int filesNumber, int[] fileSizesDistribution){

    @Override
    public String toString() {
        return "Report{" +
                "filesNumber=" + filesNumber +
                ", fileSizesDistribution=" + Arrays.toString(fileSizesDistribution) +
                '}';
    }
}