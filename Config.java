public class Config {

    public final static String WORLD_MAP_FILE_NAME = "data/map.png";
    public final static String TREE_PATTERN_FILE = "data/clue1.png";
    public final static String TREES_SAVEFILE = "data/trees.txt";
    public final static String TREES_FOLDER = "data/trees";
    public final static String MATCHES_SAVEFILE = "data/matches.png";

    // The lower this is, the faster the program searches.
    // Also makes the program run even faster if it's a power of 2.
    // However, with higher pixel tolerance, you need a larger grid size.
    // (applies to both horizontal and vertical)
    public final static int TILE_SIZE_HORIZONTAL = 16; // pixels
    public final static int TILE_SIZE_VERTICAL = 16; // pixels

    // If the tree pattern is inaccurate, you should increase this value.
    public final static double PIXEL_TOLERANCE = 10;

    // Parameters for filtering out false positives
    public final static double MAX_DISTANCE_BETWEEN_KEY_TREES = 50; // pixels
    public final static double MIN_DISTANCE_BETWEEN_KEY_TREES = 2; // pixels

    public final static double MAX_TREE_SPAN = 300; // pixels
    public final static double MAX_SCALE_RELATIVE_ERROR = 0.5; // 50% error

    public final static int DISPLAY_TOP_K_MATCHES = 40;

}
