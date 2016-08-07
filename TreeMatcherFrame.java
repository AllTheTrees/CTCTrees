import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TreeMatcherFrame extends JFrame {
    private TreeMatcherPanel treePanel;
    public TreeMatcherFrame() {

    }
    public void init() {
        buildUI();
    }

    public void buildUI() {
        try {
            treePanel = new TreeMatcherPanel(1200, 800);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setContentPane(treePanel);
        this.pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TreeMatcherFrame tmf = new TreeMatcherFrame();
                tmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                tmf.setLayout(new BorderLayout());
                tmf.setResizable(false);
                tmf.buildUI();
                tmf.setVisible(true);
            }
        });
    }
}
