import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TreeDisplayUtil {

    private static ArrayList<BufferedImage> sampleTrees;

    public static void init(final ArrayList<BufferedImage> sampleTrees) {
        TreeDisplayUtil.sampleTrees = sampleTrees;
    }

    /**
     * Highlight a tree in the display.
     * @param t
     * @param color
     * @param displayMap
     */
    public static void highlightTree(Tree t, Color color, BufferedImage displayMap) {
        Color transparent = new Color(255, 0, 255);
        BufferedImage treeImage = sampleTrees.get(t.getType());
        for (int i = 0; i < treeImage.getWidth(); i++) {
            for (int j = 0; j < treeImage.getHeight(); j++) {
                int ni = t.getX() + i, nj = t.getY() + j;
                if (ni < 0 || nj < 0 || ni >= displayMap.getWidth()
                        || nj >= displayMap.getHeight()) {
                    continue;
                }
                if (transparent.getRGB() != treeImage.getRGB(i, j)) {
                    displayMap.setRGB(ni, nj, color.getRGB());
                }
            }
        }
    }

    public static void highlightTrees(List<Tree> trees, Color color, BufferedImage displayMap) {
        for (Tree t : trees) {
            highlightTree(t, color, displayMap);
        }
    }

    public static void highlightTrees(TreeGrid treeGrid, Color color, BufferedImage displayMap) {
        highlightTrees(treeGrid.getAllTrees(), color, displayMap);
    }

    public static void drawCircleAround(List<Tree> trees, double padding, double width,
                                        Color color, BufferedImage displayMap) {
        int xave = 0, yave = 0, xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE;
        for (Tree t : trees) {
            xave += t.getX();
            yave += t.getY();
            if (t.getX() < xmin) {
                xmin = t.getX();
            }
            if (t.getY() < ymin) {
                ymin = t.getY();
            }
        }
        xave /= trees.size();
        yave /= trees.size();
        double r = Util.dist(xave, yave, xmin, ymin) + padding;
        drawCircleNaive(xave, yave, r, width, color, displayMap); // because it works
    }

    private static void drawCircleNaive(int x, int y, double r, double w,
                                        Color color, BufferedImage displayMap) {
        for (int i = (int)(x - r - w); i < x + r + w; i++) {
            for (int j = (int)(y - r - w); j < y + r + w; j++) {
                if (i < 0 || j < 0 || i >= displayMap.getWidth()
                        || j >= displayMap.getHeight()) {
                    continue;
                }
                double d = Util.dist(i, j, x, y);
                if (r < d && d < r + w) {
                    displayMap.setRGB(i, j, color.getRGB());
                } else if (d < r) {
                    Color existingColor = new Color(displayMap.getRGB(i, j));
                    Color tint = new Color(Math.min(255, existingColor.getRed() + 100),
                            Math.min(255, existingColor.getGreen() + 100),
                            Math.min(255, existingColor.getBlue() + 100));
                    displayMap.setRGB(i, j, tint.getRGB());
                }
            }
        }
    }

    public static void drawPixel(int x, int y, Color color, BufferedImage displayMap) {
        if (x < 0 || y < 0 || x >= displayMap.getWidth() || y >= displayMap.getHeight()) {
            return;
        }
        displayMap.setRGB(x, y, color.getRGB());
    }

    public static void drawVerticalLineAt(int x, int dottedLine, Color color, BufferedImage displayMap) {
        for (int y = 0; y < displayMap.getHeight(); y += dottedLine) {
            drawPixel(x, y, color, displayMap);
        }
    }

    public static void drawHorizontalLineAt(int y, int dottedLine, Color color, BufferedImage displayMap) {
        for (int x = 0; x < displayMap.getWidth(); x += dottedLine) {
            drawPixel(x, y, color, displayMap);
        }
    }
}
