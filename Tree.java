
public class Tree extends RotatablePoint {

    private int type; // tree type

    public Tree(int a, int b, int c) {
        super(a, b);
        type = c;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Tree)) {
            return false;
        }
        Tree o = (Tree)other;
        if (getX() != o.getX() || getY() != o.getY() || type != o.type) {
            return false;
        }
        return true;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return treeName(getType());
    }

    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + treeName(type) + ")";
    }

    public static String treeName(int treeType) {
        switch (treeType) {
            case 0:
                return "Oak";
            case 2:
                return "Normal";
            case 3:
                return "Dead1";
            case 4:
                return "Willow";
            case 5:
                return "Dead2";
            case 6:
                return "Bush";
            case 7:
                return "Maple";
            default:
                return "I don't know the name of this tree";
        }
    }
}

