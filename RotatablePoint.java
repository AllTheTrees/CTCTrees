
public class RotatablePoint implements Comparable<RotatablePoint> {

    private int x, y; // x and y positions

    private boolean hasXCache, hasYCache;
    private double cachedTheta, cachedXOrigin, cachedYOrigin;
    protected double cachedRotatedX, cachedRotatedY; // cached coords after being rotated

    public RotatablePoint(int a, int b) {
        x = a;
        y = b;
        hasXCache = hasYCache = false;
    }

    public int compareTo(RotatablePoint o) {
        if (x == o.x) {
            return y - o.y;
        }
        return x - o.x;
    }

    public boolean equals(Object other) {
        if (!(other instanceof RotatablePoint)) {
            return false;
        }
        RotatablePoint o = (RotatablePoint)other;
        if (getX() != o.getX() || getY() != o.getY()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (getX() + 8999 * getY()) % 999983;
    }

    public int getX() {
        return x;
    }
    public double getX(double theta) { // <-- ø
        return getX(theta, 0, 0);
    }
    public double getX(double theta, double xOrigin, double yOrigin) {
        if (hasXCache && theta == cachedTheta && xOrigin == cachedXOrigin
                && yOrigin == cachedYOrigin) {
            return cachedRotatedX;
        }
        cachedTheta = theta;
        cachedXOrigin = xOrigin;
        cachedXOrigin = yOrigin;
        hasXCache = true;
        return cachedRotatedX = xOrigin + Util.rotateX(this.x - xOrigin, this.y - yOrigin, theta);
    }

    public int getY() {
        return y;
    }
    public double getY(double theta) {
        return getY(theta, 0, 0);
    }
    public double getY(double theta, double xOrigin, double yOrigin) {
        if (hasYCache && theta == cachedTheta && xOrigin == cachedXOrigin
                && yOrigin == cachedYOrigin) {
            return cachedRotatedY;
        }
        cachedTheta = theta;
        cachedXOrigin = xOrigin;
        cachedYOrigin = yOrigin;
        hasYCache = true;
        return cachedRotatedY = yOrigin + Util.rotateY(this.x - xOrigin, this.y - yOrigin, theta);
    }

    public void setX(final int x) {
        this.x = x;
        hasXCache = hasYCache = false;
    }

    public void setY(final int y) {
        this.y = y;
        hasXCache = hasYCache = false;
    }

    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }
}
