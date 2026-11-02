import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class ImageWindow {
    private static final int REFRESH_MS = 30;

    private final BufferedImage view;
    private final int[] viewPixels;
    private final double scale;

    private ImageWindow(BufferedImage image) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        scale = Math.min(1.0, Math.min(
                screen.width * 0.9 / image.getWidth(),
                screen.height * 0.9 / image.getHeight()));
        int width = Math.max(1, (int) (image.getWidth() * scale));
        int height = Math.max(1, (int) (image.getHeight() * scale));

        view = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = view.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        viewPixels = ((DataBufferInt) view.getRaster().getDataBuffer()).getData();
    }

    public static ImageWindow open(BufferedImage image, String title)
            throws InterruptedException, InvocationTargetException {
        ImageWindow window = new ImageWindow(image);
        SwingUtilities.invokeAndWait(() -> window.showFrame(title));
        return window;
    }

    public void paintSquare(int x, int y, int width, int height, int color) {
        int left = (int) (x * scale);
        int top = (int) (y * scale);
        int right = Math.min(view.getWidth(), Math.max(left + 1, (int) ((x + width) * scale)));
        int bottom = Math.min(view.getHeight(), Math.max(top + 1, (int) ((y + height) * scale)));

        for (int row = top; row < bottom; row++) {
            int start = row * view.getWidth();
            Arrays.fill(viewPixels, start + left, start + right, color);
        }
    }

    private void showFrame(String title) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(view, 0, 0, null);
            }
        };
        panel.setPreferredSize(new Dimension(view.getWidth(), view.getHeight()));

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