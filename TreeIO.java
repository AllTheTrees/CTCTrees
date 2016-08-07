import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class TreeIO {

    public static ArrayList<BufferedImage> readTreeImages(final String folderName) throws IOException {

        ArrayList<BufferedImage> trees = new ArrayList<BufferedImage>();
        File treesFolder = new File(folderName);
        for (File treeFile : treesFolder.listFiles()) {
            if (!treeFile.isDirectory() && !treeFile.getName().substring(0, 1).equals(".")) {
                BufferedImage tree = ImageIO.read(treeFile);
                trees.add(tree);
            }
        }
        return trees;
    }

    /**
     * Load save file (with tree locations), sort and uniq them internally, return a list of unique trees
     * sorted by x and then y.
     *
     * @param fileName
     * @throws IOException
     */
    public static ArrayList<Tree> loadTrees(final String fileName) throws IOException {
        String s;
        ArrayList<Tree> trees = new ArrayList<Tree>();

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        while ((s = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s);
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int t = Integer.parseInt(st.nextToken());
            Tree tree = new Tree(x, y, t);
            trees.add(tree);
        }
        br.close();

        Collections.sort(trees, new Comparator<Tree>() {
            @Override
            public int compare(Tree a, Tree b) {
                if (a.getX() == b.getX()) {
                    return a.getY() - b.getY();
                }
                return a.getX() - b.getX();
            }
        });

        for (int i = trees.size() - 1; i > 0; i--) { // make the list unique
            Tree a = trees.get(i);
            Tree b = trees.get(i - 1);
            if (a.getX() == b.getX() && a.getY() == b.getY()) { // careful -- can't use .equals() here
                trees.remove(i);
            }
        }

        return trees;
    }

    public static void saveTrees(final List<Tree> trees, final String saveFile, boolean append) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(saveFile, append)));
        for (Tree t : trees) {
            pw.println(t.getX() + " " + t.getY() + " " + t.getType());
        }
        pw.close();
    }

    public static ArrayList<RotatablePoint> loadTreePattern(String fileName) throws IOException {
        BufferedImage clues = ImageIO.read(new File(fileName));

        ArrayList<RotatablePoint> treePattern = new ArrayList<RotatablePoint>();

        for (int i = 0; i < clues.getWidth(); i++) {
            for (int j = 0; j < clues.getHeight(); j++) {
                if (clues.getRGB(i, j) == Color.red.getRGB()) {
                    RotatablePoint p = new RotatablePoint(i, j);
                    treePattern.add(p);
                }
            }
        }

        return treePattern;
    }

    public static void saveImage(BufferedImage displayMap, String fileName) throws IOException {
        File image = new File(fileName);
        ImageIO.write(displayMap, "png", image);
    }
}
