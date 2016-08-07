import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TreeGrid {

    private final ArrayList<Tree> trees;
    private final ArrayList<Tree>[][] tiles;
    private final int width, height, tileWidth, tileHeight, numRows, numCols;

    public TreeGrid(final ArrayList<Tree> trees,
                    final int width, final int height,
                    final int tileWidth, final int tileHeight) {

        this.trees = trees;
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        this.numCols = width / tileWidth + (width % tileWidth == 0 ? 0 : 1);
        this.numRows = height / tileHeight + (height % tileHeight == 0 ? 0 : 1);

        this.tiles = new ArrayList[getNumCols()][getNumRows()];

        init();
    }

    private void init() {
        for (int i = 0; i < getNumCols(); i++) {
            for (int j = 0; j < getNumRows(); j++) {
                tiles[i][j] = new ArrayList<Tree>();
            }
        }
        for (Tree t : getAllTrees()) {
            int x = t.getX() / tileWidth, y = t.getY() / tileHeight;
            tiles[x][y].add(t);
        }
    }

    public int size() {
        return getAllTrees().size();
    }

    public Tree get(int i) {
        return getAllTrees().get(i);
    }

    public ArrayList<Tree> getAllTrees() {
        return trees;
    }

    private int getNumCols() {
        return numCols;
    }

    private int getNumRows() {
        return numRows;
    }

    public ArrayList<Tree> getTreesInSurroundingTiles(Tree t, int radius) {
        return getTreesInSurroundingTiles(t.getX(), t.getY(), radius);
    }

    public ArrayList<Tree> getTreesInSurroundingTiles(double x, double y, int radius) {
        int a = (int)(x / tileWidth), b = (int)(y / tileHeight);
        ArrayList<Tree> trees = new ArrayList<Tree>();

        for (int i = a - radius; i <= a + radius; i++) {
            if (i < 0 || i >= getNumCols()) {
                continue;
            }
            for (int j = b - radius; j <= b + radius; j++) {
                if (j < 0 || j >= getNumRows()) {
                    continue;
                }
                trees.addAll(tiles[i][j]);
            }
        }

        return trees;
    }

    public Tree findNearestTree(double x, double y, double pixelTolerance) {
        double minDist = Double.MAX_VALUE;
        Tree minTree = null;
        for (Tree t : getTreesInSurroundingTiles(x, y, 1)) {
            double dist = Util.dist(t.getX(), t.getY(), x, y);
            if (dist <= pixelTolerance && dist < minDist) {
                minDist = dist;
                minTree = t;
            }
        }
        return minTree;
    }

    public ArrayList<Tree> getTreesNearCoords(double x, double y) {
        int a = (int)(x / tileWidth), b = (int)(y / tileHeight);
        return tiles[a][b];
    }

    public void draw(Color color, BufferedImage displayMap) {
        for (int i = 1; i < getNumCols(); i++) {
            TreeDisplayUtil.drawVerticalLineAt((i - 1) * tileWidth, 3, color, displayMap);
        }
        for (int i = 1; i < getNumRows(); i++) {
            TreeDisplayUtil.drawHorizontalLineAt((i - 1) * tileHeight, 3, color, displayMap);
        }
    }

    public Tree findNearestTreeAffine(Tree a, Tree b, double ratio, double pixelTolerance) {
        double x = a.getX() * ratio + b.getX() * (1 - ratio);
        double y = a.getY() * ratio + b.getY() * (1 - ratio);
        return findNearestTree(x, y, pixelTolerance);
    }
}
