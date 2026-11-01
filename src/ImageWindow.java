import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public final class ImageWindow {
    private static final int REFRESH_MS = 30;

    private ImageWindow() {
    }

    public static void show(BufferedImage image, String title) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double scale = Math.min(1.0, Math.min(
                screen.width * 0.9 / image.getWidth(),
                screen.height * 0.9 / image.getHeight()));
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(image, 0, 0, width, height, null);
            }
        };
        panel.setPreferredSize(new Dimension(width, height));

        JFrame frame = new JFrame(title);
        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Timer(REFRESH_MS, e -> panel.repaint()).start();
    }
}