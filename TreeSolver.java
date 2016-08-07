import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class TreeSolver {

    private final BufferedImage originalMap, displayMap;
    private final boolean isTextOnly; // run in text mode

    public TreeSolver(final BufferedImage originalMap, final BufferedImage displayMap) {
        this.originalMap = originalMap;
        this.displayMap = displayMap;
        isTextOnly = this.displayMap == null;
    }

    /**
     * Phase 1 - get locations of trees from the world map
     *
     * @throws IOException
     */
    public void findTreeLocations() throws IOException {
        ArrayList<BufferedImage> sampleTrees;
        try {
            sampleTrees = loadTrees(Config.TREES_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        ArrayList<Tree> trees;
        TreeDisplayUtil.init(sampleTrees);

        try {

            File f = new File(Config.TREES_SAVEFILE);

            if (f.exists() && !f.isDirectory()) {

                // we already have a save file with all the trees' locations
                trees = TreeIO.loadTrees(Config.TREES_SAVEFILE);

            } else {
                // we need to slowly scan through to find all trees' locations
                // note that there will be many duplicates found! so `sort trees.txt | uniq` is a good idea

                TreeLocator locator = new TreeLocator(this, sampleTrees);
                locator.locateTrees(Config.TREES_SAVEFILE);
                trees = TreeIO.loadTrees(Config.TREES_SAVEFILE);
            }

            if (trees == null) {
                System.out.println("Error accessing trees file. Probably forgot to init() TreeDisplayUtil. Aborting.");
            }

            if (!isTextOnly()) {
                TreeDisplayUtil.highlightTrees(trees, Color.RED, displayMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Phase 2 - find a match with the tree pattern from the overlaid clues
     *
     * @param treePatternFileName
     * @throws IOException
     */
    public void findTreePatternMatches(String treePatternFileName, double pixelTolerance) throws IOException {

        TreeGrid treeGrid;
        try {
            ArrayList<Tree> trees = TreeIO.loadTrees(Config.TREES_SAVEFILE);
            treeGrid = new TreeGrid(trees, getWidth(), getHeight(), Config.TILE_SIZE_HORIZONTAL, Config.TILE_SIZE_VERTICAL);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        if (!isTextOnly()) {
            TreeDisplayUtil.highlightTrees(treeGrid, Color.RED, displayMap);
        }

        // Draw the grid for debugging purposes
        //treeGrid.draw(Color.WHITE, displayMap);

        try {
            ArrayList<RotatablePoint> treePattern = TreeIO.loadTreePattern(treePatternFileName);
            int numTreesInPattern = treePattern.size();
            System.out.println("Number of trees in the pattern: " + numTreesInPattern);

            double minDist = Double.MAX_VALUE;
            int mini = 0, minj = 1;
            for (int i = 0; i < numTreesInPattern; i++) {
                for (int j = i + 1; j < numTreesInPattern; j++) {
                    RotatablePoint a = treePattern.get(i);
                    RotatablePoint b = treePattern.get(j);
                    double dist = Util.dist(a, b);
                    if (dist < minDist) {
                        minDist = dist;
                        mini = i;
                        minj = j;
                    }
                }
            }
            System.out.println(mini + " " + minj);
            double theta = centerAndCalculateTheta(treePattern, mini, minj);
            double clueScale = treePattern.get(minj).getY(theta) - treePattern.get(mini).getY(theta); // change this if you're matching all rotations too

            System.out.println("Searching...");
            TreeMatcher.matchTreePatternToMap(treePattern, treeGrid, theta, clueScale, pixelTolerance, displayMap);
            TreeIO.saveImage(displayMap, Config.MATCHES_SAVEFILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTextOnly() {
        return isTextOnly;
    }

    private ArrayList<BufferedImage> loadTrees(String folderName) throws IOException {
        return TreeIO.readTreeImages(folderName);
    }

    public BufferedImage getOriginalMap() {
        return originalMap;
    }

    public BufferedImage getDisplayMap() {
        return displayMap;
    }

    public int getWidth() {
        if (originalMap.getWidth() != displayMap.getWidth()) {
            System.out.println("Something's wrong: widths don't match up");
        }
        return originalMap.getWidth();
    }

    public int getHeight() {
        if (originalMap.getHeight() != displayMap.getHeight()) {
            System.out.println("Something's wrong: heights don't match up");
        }
        return originalMap.getHeight();
    }

    /**
     * Theta is the angle to rotate the tree pattern such that tree A and tree B are vertically aligned.
     * We later rotate all the trees in the world map by the same theta.
     * Having a pair of vertically aligned trees makes searching for matches easier (hopefully).
     *
     * If we want to include rotations of the tree pattern in our matches, however, then doing these rotations
     * is optional since we will have to brute force search later anyways.
     *
     * Theta does not have anything to do with the thetas on the clues. ._.
     *
     * Also centers trees in treePattern at tree A as a side effect. (Tree A will be at the origin.)
     *
     * assert(keyAIndex < keyBIndex !!!)
     *
     * @param treePattern
     * @param keyAIndex
     * @param keyBIndex
     * @return
     */
    private double centerAndCalculateTheta(ArrayList<RotatablePoint> treePattern, int keyAIndex, int keyBIndex) {

        RotatablePoint a = treePattern.get(keyAIndex);
        RotatablePoint b = treePattern.get(keyBIndex);

        // Calculate theta, which will vertically align A and B, with A on "top" (lower y value)
        double theta = Math.atan2(a.getY() - b.getY(), b.getX() - a.getX());
        if (b.getX() > a.getX()) {
            theta += Math.PI / 2;
        } else {
            theta -= Math.PI / 2;
        }

        // Center the trees at A
        int ax = a.getX();
        int ay = a.getY();
        for (RotatablePoint tree : treePattern) {
            tree.setX(tree.getX() - ax);
            tree.setY(tree.getY() - ay);
        }

        return theta;
    }
}
