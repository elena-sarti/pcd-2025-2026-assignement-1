package pcd.sequentialVersion.model;

public record Boundary(double x0, double y0, double x1, double y1){

    public double getWidth() {
        return Math.abs(x1 - x0);
    }

    public double getHeight() {
        return Math.abs(y1 - y0);
    }
}
