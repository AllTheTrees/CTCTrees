import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class TreeLocator {

    private final TreeSolver solver;
    private final ArrayList<BufferedImage> sampleTrees;

    private final Color OCEAN_COLOR = new Color(70, 88, 139);
    private final Color TRANSPARENT = new Color(255, 0, 255);

    private final double TREE_MATCH_THRESHOLD = 0.8; // percentage of pixels correct for it to be counted as a match

    public TreeLocator(final TreeSolver solver, final ArrayList<BufferedImage> sampleTrees) {
        this.solver = solver;
        this.sampleTrees = sampleTrees;
    }

    public void locateTrees(String saveFile) throws IOException {
        int windowWidth = 200, windowHeight = 200; // sliding window

        for (int i = 0; i < solver.getWidth(); i += windowWidth) {
            for (int j = 0; j < solver.getHeight(); j += windowHeight) {
                findTrees(i, j, windowWidth, windowHeight, saveFile);
            }
        }

        // TODO: add last row / column, if you want
    }

    private Color getOriginalMapColor(int x, int y) {
        return new Color(solver.getOriginalMap().getRGB(x, y));
    }

    private boolean isOceanOrVoid(int x, int y) {
        Color c = getOriginalMapColor(x, y);
        return OCEAN_COLOR.equals(c) || Color.BLACK.equals(c);
    }

    private boolean isOceanOrVoidRegion(int xstart, int ystart, int xend, int yend) {
        if (!(isOceanOrVoid(xstart, ystart) && isOceanOrVoid(xstart, yend)
                && isOceanOrVoid(xend, yend) && isOceanOrVoid(xend, ystart)
                && isOceanOrVoid((xstart + xend) / 2, (ystart + yend) / 2))) {
            return false;
        }
        for (int i = 0; i < 20; i++) { // randomized for now
            int xrand = (int)(Math.random() * (xend - xstart)) + xstart;
            int yrand = (int)(Math.random() * (yend - ystart)) + ystart;
            if (!isOceanOrVoid(xrand, yrand)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Naively find the trees in the given window of the map
     * @param xstart
     * @param ystart
     * @param windowWidth
     * @param windowHeight
     * @param displayImage // TODO: maybe add this later?
     * @param saveFile
     * @return true if not ocean / empty space on the map
     */
    private boolean findTrees(int xstart, int ystart,
                              int windowWidth, int windowHeight, String saveFile) throws IOException {
        int endWidth = Math.min(solver.getWidth(), xstart + windowWidth);
        int endHeight = Math.min(solver.getHeight(), ystart + windowHeight);

        if (isOceanOrVoidRegion(xstart, ystart, endWidth - 1, endHeight - 1)) { // skip ocean / black sections of map
            return false;
        }

        ArrayList<Tree> foundTrees = new ArrayList<Tree>();
        for (int j = ystart; j < endHeight; j++) {
            for (int i = xstart; i < endWidth; i++) {
                if (findTreesAt(i, j, foundTrees)) {
                    i += 2; // hacky way to enforce horizontal space between trees
                }
            }
        }

        TreeIO.saveTrees(foundTrees, saveFile, true); // append
        return true;
    }

    private boolean findTreesAt(int x, int y, ArrayList<Tree> foundTrees) {
        int type = -1;
        double maxScore = 0;
        int maxType = 0;
        for (BufferedImage sampleTree : sampleTrees) {
            type++;
            double score = scoreTreeAt(x, y, sampleTree);
            if (score > TREE_MATCH_THRESHOLD && score > maxScore) {
                maxScore = score;
                maxType = type;
            }
        }
        if (maxScore > TREE_MATCH_THRESHOLD) {
            foundTrees.add(new Tree(x, y, maxType));
            return true;
        }
        return false;
    }

    private double scoreTreeAt(int x, int y, BufferedImage sampleTree) {
        int numPixels = 0, tally = 0;
        for (int i = 0; i < sampleTree.getWidth(); i++) {
            for (int j = 0; j < sampleTree.getHeight(); j++) {
                if (x + i >= solver.getWidth() || y + j >= solver.getHeight()) {
                    return -1;
                }
                Color t = new Color(sampleTree.getRGB(i, j));
                if (TRANSPARENT.equals(t)) {
                    continue;
                }
                Color c = getOriginalMapColor(x + i, y + j);
                if (c.equals(t)) {
                    tally++;
                }
                numPixels++;
            }
        }
        return tally / (double)numPixels;
    }
}

