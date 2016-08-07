import java.util.ArrayList;

public class TreeMatch implements Comparable<TreeMatch> {
    private ArrayList<Tree> matchingTrees;
    private double normalizedSquaredError;

    public TreeMatch(final ArrayList<Tree> matchingTrees, final double normalizedSquaredError) {
        this.matchingTrees = matchingTrees;
        this.normalizedSquaredError = normalizedSquaredError;
    }

    public int compareTo(TreeMatch other) {
        if (normalizedSquaredError == other.normalizedSquaredError) {
            return 0;
        } else if (normalizedSquaredError > other.normalizedSquaredError) {
            return 1;
        }
        return -1;
    }

    public int hashCode() {
        return super.hashCode();
        // implement this later if you want to add double matching scores
    }

    public ArrayList<Tree> getMatchingTrees() {
        return matchingTrees;
    }

    public double getNormalizedSquaredError() {
        return normalizedSquaredError;
    }
}
