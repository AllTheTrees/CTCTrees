import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TreeMatcherPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private BufferedImage bi;        // "true" unmodified world map
    private BufferedImage displaybi; // the world map that we are displaying
    private int width, height;       // of the window
    private int xpos, ypos;          // of the location in the image at the top left of the window
    private double scale;            // level of zoom

    private int xclicked, yclicked;  // location of most recent mouse press
    private int xposprev, yposprev;  // xpos and ypos before the last mouse press

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("Clicked " + (int)(xpos + scale * e.getX()) + " " + (int)(ypos + scale * e.getY()));
    }

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

    public TreeMatcherPanel(int w, int h) throws IOException {
        bi = ImageIO.read(new File(Config.WORLD_MAP_FILE_NAME));
        displaybi = new BufferedImage(bi.getColorModel(), bi.copyData(null),
                bi.isAlphaPremultiplied(), null);

        width = w;
        height = h;
        xpos = 3780;
        ypos = 1610;
        scale = 1;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        // offload heavy duty processing to worker thread
        // so as to reduce lag in the interface
        new TreeLocatorWorker().execute();
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.drawImage(displaybi.getSubimage(xpos, ypos, (int) (scale * width),
                (int) (scale * height)), 0, 0, width, height, null);
    }

    private class TreeLocatorWorker extends SwingWorker<Void, BufferedImage> {

        @Override
        protected Void doInBackground() throws Exception {
            BufferedImage displayMap = new BufferedImage(bi.getColorModel(), bi.copyData(null),
                    bi.isAlphaPremultiplied(), null);

            TreeSolver ts = new TreeSolver(bi, displayMap);

            ts.findTreeLocations();
            publish(displayMap);

            ts.findTreePatternMatches(Config.TREE_PATTERN_FILE, Config.PIXEL_TOLERANCE);
            publish(displayMap);

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
