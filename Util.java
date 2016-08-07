import java.util.List;

public class Util {

    public static double rotateX(double x, double y, double theta) {
        return x * Math.cos(theta) - y * Math.sin(theta);
    }

    public static double rotateY(double x, double y, double theta) {
        return x * Math.sin(theta) + y * Math.cos(theta);
    }

    public static double dist(double a, double b, double x, double y) {
        return Math.hypot(a - x, b - y);
    }

    public static double dist(RotatablePoint a, RotatablePoint b) {
        return dist(a.getX(), a.getY(), b.getX(), b.getY());
    }

    /**
     * Largest distance between points
     * @param points
     * @return
     */
    public static double diameter(List<? extends RotatablePoint> points) {
        double maxDist = 0;
        for (RotatablePoint a : points) {
            for (RotatablePoint b : points) {
                double distance = dist(a, b);
                if (distance > maxDist) {
                    maxDist = distance;
                }
            }
        }
        return maxDist;
    }
}
