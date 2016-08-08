public class Config {

    public static String WORLD_MAP_FILE_NAME = "data/map.png";
    public static String TREE_PATTERN_FILE = "data/pattern.png";
    public static String TREES_SAVEFILE = "data/trees.txt";
    public static String TREES_FOLDER = "data/trees";
    public static String MATCHES_SAVEFILE = "data/matches.png";

    // The lower this is, the faster the program searches.
    // Also makes the program run even faster if it's a power of 2.
    // However, with higher pixel tolerance, you need a larger grid size.
    // (applies to both horizontal and vertical)
    public static int TILE_SIZE_HORIZONTAL = 16; // pixels
    public static int TILE_SIZE_VERTICAL = 16; // pixels

    // If the tree pattern is inaccurate, you should increase this value.
    public static double PIXEL_TOLERANCE = 8;

    // Parameters for filtering out false positives
    public static double MAX_DISTANCE_BETWEEN_KEY_TREES = 50; // pixels
    public static double MIN_DISTANCE_BETWEEN_KEY_TREES = 2; // pixels

    public static double MAX_TREE_SPAN = 300; // pixels
    public static double MAX_SCALE_RELATIVE_ERROR = 0.3; // 30% error

    public static int DISPLAY_TOP_K_MATCHES = 50;

}
