import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TreeLocator extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private BufferedImage bi;
    private BufferedImage displaybi;
    private int width, height;
    private int xpos, ypos;
    private double scale;
    private int xclicked, yclicked;
    private int xposprev, yposprev;

    @Override
    public void mouseClicked(MouseEvent e) { /* Do nothing */ }

    @Override
    public void mousePressed(MouseEvent e) {
        xclicked = e.getX();
        yclicked = e.getY();
        xposprev = xpos;
        yposprev = ypos;
    }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) {
        xpos = xposprev - (int)((e.getX() - xclicked) * scale);
        ypos = yposprev - (int)((e.getY() - yclicked) * scale);
        if (xpos < 0) {
            xpos = 0;
        }
        if (ypos < 0) {
            ypos = 0;
        }
        if (xpos > bi.getWidth() - (int)(scale * width)) {
            xpos = bi.getWidth() - (int)(scale * width);
        }
        if (ypos > bi.getHeight() - (int)(scale * height)) {
            ypos = bi.getHeight() - (int)(scale * height);
        }
        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scale += e.getWheelRotation() * 0.05;
        if (scale < 0.03) {
            scale = 0.03;
        }
        if (scale > 2) {
            scale = 2;
        }
        if (xpos > bi.getWidth() - (int)(scale * width)) {
            xpos = bi.getWidth() - (int)(scale * width);
            if (xpos < 0) {
                xpos = 0;
                scale = Math.floor(bi.getWidth() / (double)width);
            }
        }
        if (ypos > bi.getHeight() - (int)(scale * height)) {
            ypos = bi.getHeight() - (int)(scale * height);
            if (ypos < 0) {
                ypos = 0;
                scale = Math.floor(bi.getHeight() / (double)height);
            }
        }
        this.repaint();
    }

    public TreeLocator(int w, int h) throws IOException {
        bi = ImageIO.read(new File("data/map.png"));
        displaybi = new BufferedImage(bi.getColorModel(), bi.copyData(null), bi.isAlphaPremultiplied(), null);
        width = w;
        height = h;
        //xpos = ypos = 0;
        xpos = 3780;
        ypos = 1610;
        scale = 0.2;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        new TreeLocatorWorker().execute();
    }
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    public void paint(Graphics g) {
        g.drawImage(displaybi.getSubimage(xpos, ypos, (int) (scale * width), (int) (scale * height)), 0, 0, width, height, null);
    }

    private static class Tree implements Comparable<Tree> {
        private int x, y, treeType;
        public Tree(int a, int b, int c) {
            x = a;
            y = b;
            treeType = c;
        }
        public int compareTo(Tree other) {
            if (x == other.x) {
                return y - other.y;
            }
            return x - other.x;
        }
        public boolean equals(Object other) {
            if (!(other instanceof Tree)) {
                return false;
            }
            Tree o = (Tree)other;
            if (x != o.x || y != o.y || treeType != o.treeType) {
                return false;
            }
            return true;
        }
    }

    private class TreeLocatorWorker extends SwingWorker<Void, BufferedImage> {

        private ArrayList<BufferedImage> trees;
        private final Color water = new Color(70, 88, 139);
        private final Color black = new Color(0, 0, 0);
        private ArrayList<Tree> treeLocations;

        private void loadTrees() throws IOException {
            trees = new ArrayList<BufferedImage>();
            File treesFolder = new File("data/trees");
            for (File treeFile : treesFolder.listFiles()) {
                BufferedImage tree = ImageIO.read(treeFile);
                trees.add(tree);
            }
        }

        private boolean isWaterOrVoid(int x, int y) {
            int rgb = bi.getRGB(x, y);
            return rgb == water.getRGB() || rgb == black.getRGB();
        }

        private boolean isWaterOrVoidRegion(int xstart, int ystart, int xend, int yend) {
            if (!(isWaterOrVoid(xstart, ystart) && isWaterOrVoid(xstart, yend)
                    && isWaterOrVoid(xend, yend) && isWaterOrVoid(xend, ystart)
                    && isWaterOrVoid((xstart + xend) / 2, (ystart + yend) / 2))) {
                return false;
            }
            for (int i = 0; i < 20; i++) { // randomized
                int xrand = (int)(Math.random() * (xend - xstart)) + xstart;
                int yrand = (int)(Math.random() * (yend - ystart)) + ystart;
                if (!isWaterOrVoid(xrand, yrand)) {
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
         * @param displayImage
         * @param pw
         * @return true if not ocean / empty space on the map
         */
        private boolean findTrees(int xstart, int ystart, int windowWidth, int windowHeight, BufferedImage displayImage,
                                  PrintWriter pw) {
            ArrayList<Tree> foundTrees = new ArrayList<Tree>();
            Color transparent = new Color(255, 0, 255);
            int endWidth = Math.min(bi.getWidth(), xstart + windowWidth);
            int endHeight = Math.min(bi.getHeight(), ystart + windowHeight);

            if (isWaterOrVoidRegion(xstart, ystart, endWidth - 1, endHeight - 1)) { // skip ocean / black sections of map
                return false;
            }

            for (int j = ystart; j < endHeight; j++) {
                for (int i = xstart; i < endWidth; i++) {
                    int type = -1;
                    double maxScore = 0;
                    int maxType = 0;
                    for (BufferedImage tree : trees) {
                        type++;
                        double score = 0;
                        int numPixels = 0;
                        for (int k = 0; k < tree.getWidth(); k++) {
                            for (int p = 0; p < tree.getHeight(); p++) {
                                if (i + k >= bi.getWidth() || j + p >= bi.getHeight()) {
                                    score = -1;
                                    break;
                                }
                                Color t = new Color(tree.getRGB(k, p));
                                if (transparent.getRGB() == t.getRGB()) {
                                    continue;
                                }
                                Color c = new Color(bi.getRGB(i + k, j + p));
                                if (c.getRGB() == t.getRGB()) {
                                    score += 1;
                                }
                                numPixels++;
                            }
                        }
                        score /= numPixels;
                        if (score > 0.8 && score > maxScore) {
                            maxScore = score;
                            maxType = type;
                        }
                    }
                    if (maxScore > 0.8) {
                        foundTrees.add(new Tree(i, j, maxType));
                        i += 2; // hacky way to enforce horizontal space between trees
                    }
                }
            }

            for (Tree t : foundTrees) {
                colorTree(t.x, t.y, t.treeType, Color.red, displayImage);
            }
            return true;
        }

        private ArrayList<Point> scanClues() throws IOException {
            BufferedImage clues = ImageIO.read(new File("data/clues1.png"));
            ArrayList<Point> clueLocs = new ArrayList<Point>();

            for (int i = 0; i < clues.getWidth(); i++) {
                for (int j = 0; j < clues.getHeight(); j++) {
                    if (clues.getRGB(i, j) == Color.red.getRGB()) {
                        Point p = new Point(i - 394, j - 305); // for use with clues1.png
                        //Point p = new Point(i - 394, j - 263); // for use with clues2.png
                        clueLocs.add(p);
                    }
                }
            }

            return clueLocs;
        }

        /**
         * Fuzzy binary search over sorted trees list - helper
         * @param x
         * @param y
         * @param l
         * @param h
         * @param fuzz
         * @param sortedTrees
         * @return
         */
        private Integer searchHelper(int x, int y, int l, int h, int fuzz, ArrayList<Tree> sortedTrees) {
            // exact match x, fuzzy match y
            if (l > h) {
                return null;
            }
            int m = (l + h) / 2;
            Tree mid = sortedTrees.get(m);
            if (x == mid.x) {
                if (Math.abs(y - mid.y) <= fuzz) {
                    return m;
                } else if (y > mid.y) {
                    return searchHelper(x, y, m + 1, h, fuzz, sortedTrees);
                } else {
                    return searchHelper(x, y, l, m - 1, fuzz, sortedTrees);
                }
            }
            else if (x > mid.x) {
                return searchHelper(x, y, m + 1, h, fuzz, sortedTrees);
            } else {
                return searchHelper(x, y, l, m - 1, fuzz, sortedTrees);
            }
        }

        private Integer search(int x, int y, int fuzz, ArrayList<Tree> sortedTrees) {
            // exact match x, fuzzy match y
            return searchHelper(x, y, 0, sortedTrees.size() - 1, fuzz, sortedTrees);
        }

        /**
         * Fuzzy binary search over sorted trees list
         * @param x
         * @param y
         * @param fuzz
         * @param sortedTrees
         * @return
         */
        private Integer treeNear(int x, int y, int fuzz, ArrayList<Tree> sortedTrees) {
            // fuzzy match both x and y
            for (int i = -fuzz; i <= fuzz; i++) {
                Integer index = search(x + i, y, fuzz, sortedTrees);
                if (index != null) {
                    return index;
                }
            }
            return null;
        }

        private double dist(int a, int b, int x, int y) {
            return Math.sqrt((a - x) * (a - x) + (b - y) * (b - y));
        }

        private void drawCircleNaive(int x, int y, int r, int w, Color color, BufferedImage displayImage) {
            for (int i = x - r - w; i < x + r + w; i++) {
                for (int j = y - r - w; j < y + r + w; j++) {
                    if (i < 0 || j < 0 || i >= displayImage.getWidth() || j >= displayImage.getHeight()) {
                        continue;
                    }
                    double d = dist(i, j, x, y);
                    if (r < d && d < r + w) {
                        displayImage.setRGB(i, j, color.getRGB());
                    }
                }
            }
        }

        private void drawCircleAround(List<Integer> treeIndices, ArrayList<Tree> sortedTrees, Color color, BufferedImage displayImage) {
            int xave = 0, yave = 0, xmin = Integer.MAX_VALUE, ymin = Integer.MAX_VALUE;
            for (int i : treeIndices) {
                Tree t = sortedTrees.get(i);
                xave += t.x;
                yave += t.y;
                if (t.x < xmin) {
                    xmin = t.x;
                }
                if (t.y < ymin) {
                    ymin = t.y;
                }
            }
            xave /= treeIndices.size();
            yave /= treeIndices.size();
            int r = (int)dist(xave, yave, xmin, ymin) + 20;
            drawCircleNaive(xave, yave, r, 5, color, displayImage); // because it works
        }

        /**
         * Given two vertically aligned trees, check whether or not there are other trees in
         * the locations according to the stacked clues (clues1.png or clues2.png)
         * @param indexA
         * @param indexB
         * @param clues
         * @param sortedTrees
         * @param displayImage
         * @return
         */
        private boolean matchTreesToMap(int indexA, int indexB, ArrayList<Point> clues, ArrayList<Tree> sortedTrees, BufferedImage displayImage) {
            Tree treeA = sortedTrees.get(indexA);
            Tree treeB = sortedTrees.get(indexB);
            ArrayList<Integer> matchingTreeIndices = new ArrayList<Integer>();
            double scale = (treeB.y - treeA.y) / 42.; // magic number: distance between "key trees" in clue

            int fuzz = 5; // error threshold of tree locations (in pixels)

            for (Point p : clues) {
                Integer treeIndex = treeNear(treeA.x + (int)(p.getX() * scale), treeA.y + (int)(p.getY() * scale), fuzz, sortedTrees);
                if (treeIndex != null) { // there is a tree near this point in the world map
                    matchingTreeIndices.add(treeIndex);
                }
            }
            if (matchingTreeIndices.size() >= 18) { // near-perfect match
                System.out.print("Found a match of " + matchingTreeIndices.size() + " trees! (some trees may be counted twice) ");
                //Color randomColor = new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
                Color randomColor = Color.green;
                for (Integer x : matchingTreeIndices) {
                    Tree t = sortedTrees.get(x);
                    colorTree(t.x, t.y, t.treeType, randomColor, displayImage);
                }
                Tree t = sortedTrees.get(matchingTreeIndices.get(0));
                System.out.println(t.x + " " + t.y);
                drawCircleAround(matchingTreeIndices, sortedTrees, randomColor, displayImage);
                return true;
            }
            return false;
        }

        /**
         * Match the trees in the stacked clues to the trees on the world map
         * @param clues
         * @param sortedTrees
         * @param displayImage
         */
        private void matchCluesToMap(ArrayList<Point> clues, ArrayList<Tree> sortedTrees, BufferedImage displayImage) {
            System.out.println("Searching...");
            if (sortedTrees == null) {
                return;
            }
            for (int i = 0; i < sortedTrees.size() - 1; i++) {
                Tree tree = sortedTrees.get(i);
                for (int j = i + 1; j < sortedTrees.size(); j++) {
                    Tree nextTree = sortedTrees.get(j);
                    if (nextTree.x == tree.x) {
                        if (matchTreesToMap(i, j, clues, sortedTrees, displayImage)) {
                            //break; // don't really need to break, break if you want
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        /**
         * Color in a tree in the display.
         * @param x
         * @param y
         * @param treeType
         * @param color
         * @param displayImage
         */
        private void colorTree(int x, int y, int treeType, Color color, BufferedImage displayImage) {
            Color transparent = new Color(255, 0, 255);
            BufferedImage treeImage = trees.get(treeType);
            for (int i = 0; i < treeImage.getWidth(); i++) {
                for (int j = 0; j < treeImage.getHeight(); j++) {
                    if (transparent.getRGB() != treeImage.getRGB(i, j)) {
                        displayImage.setRGB(x + i, y + j, color.getRGB());
                    }
                }
            }
        }

        /**
         * Load save file (with tree locations), sort and uniq them internally, populate treeLocations: a sorted list of trees
         * @param saveFile
         * @param displayImage
         * @throws IOException
         */
        private void loadSaveFile(String saveFile, BufferedImage displayImage) throws IOException {
            BufferedReader br = new BufferedReader(new FileReader(saveFile));
            String s;
            treeLocations = new ArrayList<Tree>();
            while ((s = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(s);
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                int t = Integer.parseInt(st.nextToken());
                Tree tree = new Tree(x, y, t);
                treeLocations.add(tree);
                colorTree(x, y, t, Color.red, displayImage);
                trees.get(t);
            }
            br.close();
            Collections.sort(treeLocations);
            for (int i = treeLocations.size() - 1; i > 0; i--) { // make the list unique
                if (treeLocations.get(i).equals(treeLocations.get(i - 1))) {
                    treeLocations.remove(i);
                }
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("trees_sorted_final.txt")));
            for (Tree t : treeLocations) {
                pw.println(t.x + " " + t.y + " " + t.treeType);
            }
            pw.close();
            publish(displayImage);
        }

        @Override
        protected Void doInBackground() throws Exception {

            /* Phase 1 - get locations of trees from the world map */

            BufferedImage displayImage;
            displayImage = new BufferedImage(displaybi.getColorModel(), displaybi.copyData(null),
                    displaybi.isAlphaPremultiplied(), null);
            String saveFile = "data/trees.txt";

            try {
                loadTrees();
                File f = new File(saveFile);
                if (f.exists() && !f.isDirectory()) { // we already have a save file with all the trees' locations
                    loadSaveFile(saveFile, displayImage);
                } else { // we need to slowly scan through to find all trees' locations
                    // note that there will be many duplicates found! so `sort trees.txt | uniq` is a good idea

                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile)));

                    int windowWidth = 200, windowHeight = 200; // sliding window

                    for (int i = 0; i < bi.getWidth(); i += windowWidth - 6) { // account for trees on border
                        for (int j = 0; j < bi.getHeight(); j += windowHeight - 6) {
                            if (findTrees(i, j, windowWidth, windowHeight, displayImage, pw)) {
                                publish(displayImage);
                                Thread.sleep(50);
                            }
                        }
                    }
                    pw.close();
                    File allTrees = new File("data/allTrees.png");
                    ImageIO.write(displayImage, "png", allTrees);
                    loadSaveFile(saveFile, displayImage);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            /* Phase 2 - find a match with the tree pattern from the overlaid clues */

            try {
                matchCluesToMap(scanClues(), treeLocations, displayImage);
                publish(displayImage);

                File treeMatches = new File("data/treeMatches.png");
                ImageIO.write(displayImage, "png", treeMatches);
            } catch (Exception e) {
                System.out.println(e);
            }

            return null;

        }

        @Override
        protected void process(List<BufferedImage> displayImages) {
            BufferedImage displayImage = displayImages.get(displayImages.size() - 1);
            displaybi = new BufferedImage(displayImage.getColorModel(), displayImage.copyData(null),
                    displayImage.isAlphaPremultiplied(), null);
            repaint();
        }

        @Override
        protected void done() {
            System.out.println("done!");
        }
    }
}
