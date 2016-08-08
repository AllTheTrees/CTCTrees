import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class TreeMatcher {
    /**
     * Match the trees in the stacked clues to the trees on the world map
     *
     * @param treePattern
     * @param treeGrid
     * @param theta
     * @param patternScale
     * @param pixelTolerance
     * @param displayMap
     */
    public static void matchTreePatternToMap(ArrayList<RotatablePoint> treePattern, TreeGrid treeGrid,
                                             double theta, double patternScale, double pixelTolerance, BufferedImage displayMap) {
        ArrayList<TreeMatch> matches = new ArrayList<TreeMatch>();
        for (int i = 0; i < treeGrid.size() - 1; i++) {
            if (i % 1000 == 0) {
                System.out.println((int)(i / (double)treeGrid.size() * 100) + "%");
            }
            Tree treeA = treeGrid.get(i);
            ArrayList<Tree> treesNearby = treeGrid.getTreesInSurroundingTiles(treeA, 1);

            for (Tree treeB : treesNearby) {
                if (treeA == treeB) continue;
                if (Math.abs(treeA.getX(theta) - treeB.getX(theta)) <= pixelTolerance
                        && treeB.getY(theta) > treeA.getY(theta)) {

                    double distance = Util.dist(treeA, treeB);
                    if (Config.MIN_DISTANCE_BETWEEN_KEY_TREES < distance && distance < Config.MAX_DISTANCE_BETWEEN_KEY_TREES) {
                        TreeMatch match = matchTreesToMap(treeA, treeB, treePattern, treeGrid, theta, patternScale, pixelTolerance);
                        if (match != null) {
                            matches.add(match);
                        }
                    }
                }
            }
        }

        Collections.sort(matches);
        System.out.println("Found " + matches.size() + " matches.");
        if (matches.size() > 0) {
            System.out.println("Showing the first " + Math.min(matches.size(), Config.DISPLAY_TOP_K_MATCHES) + " matches.");
        }
        for (int i = Math.min(matches.size() - 1, Config.DISPLAY_TOP_K_MATCHES); i >= 0; i--) {
            // The higher the score, the more green it gets
            int greenAmount = (int)(1 / (i / (Config.DISPLAY_TOP_K_MATCHES / 4.) + 1.) * 255.);
            Color scaledColor = new Color(0, greenAmount, 255 - greenAmount);

            TreeDisplayUtil.drawCircleAround(matches.get(i).getMatchingTrees(), 20, 5, scaledColor, displayMap);
            for (Tree tree : matches.get(i).getMatchingTrees()) {
                TreeDisplayUtil.highlightTree(tree, scaledColor, displayMap);
            }
        }
    }

    /**
     * Given two vertically aligned trees, check whether or not there are other trees in
     * the locations according to the tree pattern.
     *
     * @param treeA
     * @param treeB
     * @param treePattern
     * @param treeGrid
     * @param theta
     * @param patternScale
     * @param pixelTolerance error tolerance of each tree's position (in pixels)
     * @return
     */
    public static TreeMatch matchTreesToMap(Tree treeA, Tree treeB, ArrayList<RotatablePoint> treePattern,
                                          TreeGrid treeGrid, double theta, double patternScale,
                                          double pixelTolerance) {

        ArrayList<Tree> matchingTrees = new ArrayList<Tree>();

        // Calculate scaling factor to convert from tree pattern dimensions to pixels
        double treeScale = (treeB.getY(theta) - treeA.getY(theta));
        double scale = treeScale / patternScale;

        // Check closest trees are at least 1 pixel apart
        double dist = Util.diameter(treePattern);
        if (dist * scale < 1) {
            return null;
        }

        // Find trees in the area matching the pattern, and
        // calculate the x and y shift to minimize squared error
        double meanX = 0, meanY = 0, normalizedSquaredError = 0, numTreesFound = 0;
        for (RotatablePoint point : treePattern) {
            double estimatedX = treeA.getX() + point.getX() * scale;
            double estimatedY = treeA.getY() + point.getY() * scale;
            Tree nearestTree = treeGrid.findNearestTree(estimatedX, estimatedY, pixelTolerance);

            if (nearestTree == null) { // there is no tree near this point in the world map
                return null;
            } else {
                numTreesFound++;
                meanX += nearestTree.getX() - estimatedX;
                meanY += nearestTree.getY() - estimatedY;
                matchingTrees.add(nearestTree);
            }
        }
        meanX /= numTreesFound;
        meanY /= numTreesFound;

        // Calculate normalized squared error after shifting by the mean x and y
        for (RotatablePoint point : treePattern) {
            double estimatedX = treeA.getX() + point.getX() * scale;
            double estimatedY = treeA.getY() + point.getY() * scale;
            Tree nearestTree = treeGrid.findNearestTree(estimatedX, estimatedY, pixelTolerance);

            if (nearestTree != null) {
                double centeredDist = Util.dist(estimatedX + meanX, estimatedY + meanY,
                        nearestTree.getX(), nearestTree.getY());
                normalizedSquaredError += centeredDist * centeredDist / treeScale / treeScale;
            }
        }

        if (!runChecks(matchingTrees, treePattern, treeGrid, scale)) {
            return null;
        }

        return new TreeMatch(matchingTrees, normalizedSquaredError);
    }

    private static boolean checkTreeBIsPresent(ArrayList<Tree> matchingTrees, Tree treeB) {
        // Tree B check - treeB is likely one of the matched trees
        for (Tree matchingTree : matchingTrees) {
            if (matchingTree == treeB) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkTreesInVicinity(ArrayList<Tree> matchingTrees, TreeGrid treeGrid) {
        HashSet<Tree> matchingTreesSet = new HashSet<Tree>(matchingTrees);

        int numTreesInBetween = 0, threshold = (int)(matchingTrees.size() * 0.1);

        // Find trees at affine combinations of matching trees
        for (int i = 0; i < matchingTrees.size(); i++) {
            for (int j = i + 1; j < matchingTrees.size(); j++) {
                Tree a = matchingTrees.get(i);
                Tree b = matchingTrees.get(j);
                double dist = Util.dist(a, b);
                int numberOfPointsToCheck = (int)Math.max(10, dist / 2);
                for (int k = 1; k < numberOfPointsToCheck - 1; k++) { // find trees on the way from a to b
                    Tree treeInBetween = treeGrid.findNearestTreeAffine(a, b,
                            k / (double)numberOfPointsToCheck, 2 + 1e-10);

                    if (treeInBetween != null && !matchingTreesSet.contains(treeInBetween)) {
                        if (numTreesInBetween++ >= threshold) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean checkDistances(ArrayList<Tree> matchingTrees,
                                  ArrayList<RotatablePoint> treePattern, double scale) {
        // Max distance check - don't want the match to be too big
        double dist = Util.diameter(matchingTrees);
        if (dist > Config.MAX_TREE_SPAN) {
            return false;
        }

        // Approximate distance check - check the span is approximately that in the clues
        double treePatternDist = Util.diameter(treePattern) * scale;
        return Math.abs(dist - treePatternDist) / treePatternDist <= Config.MAX_SCALE_RELATIVE_ERROR;
    }

    private static boolean checkMatchesAreMostlyUnique(ArrayList<Tree> matchingTrees, ArrayList<RotatablePoint> treePattern) {
        // Unique count -- about the same size as the tree pattern
        int unique = 1;
        Collections.sort(matchingTrees);
        for (int i = 0; i < matchingTrees.size() - 1; i++) {
            if (!matchingTrees.get(i).equals(matchingTrees.get(i + 1))) {
                unique++;
            }
        }
        int matchingTolerance = (int)(treePattern.size() * 0.2);
        int uniqueTolerance = (int)(matchingTrees.size() * 0.2);
        return matchingTrees.size() >= treePattern.size() - matchingTolerance &&
            unique >= matchingTrees.size() - uniqueTolerance;
    }

    /**
     * Check that the match is not a false positive
     * @param matchingTrees
     * @param treePattern
     * @param treeGrid
     * @param scale
     * @return
     */
    private static boolean runChecks(ArrayList<Tree> matchingTrees, ArrayList<RotatablePoint> treePattern,
                             TreeGrid treeGrid, double scale) {

        if (!checkMatchesAreMostlyUnique(matchingTrees, treePattern)) {
            return false;
        }
        if (!checkTreesInVicinity(matchingTrees, treeGrid)) {
            return false;
        }
        if (!checkDistances(matchingTrees, treePattern, scale)) {
            return false;
        }
        return true;
    }
}
