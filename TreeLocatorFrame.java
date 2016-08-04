import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TreeLocatorFrame extends JFrame {
    private TreeLocator tl;
    public TreeLocatorFrame() {

    }
    public void init() {
        buildUI();
    }

    public void buildUI() {
        try {
            tl = new TreeLocator(1200, 800);
        } catch (IOException e) {
            //swallow
        }
        this.setContentPane(tl);
        this.pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TreeLocatorFrame tla = new TreeLocatorFrame();
                tla.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                tla.setLayout(new BorderLayout());
                tla.setResizable(false);
                tla.buildUI();
                tla.setVisible(true);
            }
        });
    }
}
