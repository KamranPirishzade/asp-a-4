import java.io.File;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Main {
    private static final String USAGE = "Usage: java -cp out Main <image.jpg> <square size> <S|M>";

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            fail("Expected 3 arguments.");
        }

        File file = new File(args[0]);
        if (!file.isFile()) {
            fail("File not found: " + args[0]);
        }

        int size = parseSize(args[1]);

        String mode = args[2].toUpperCase();
        if (!mode.equals("S") && !mode.equals("M")) {
            fail("Mode must be S (single-threaded) or M (multi-threaded).");
        }

        BufferedImage image = loadImage(file);
        SwingUtilities.invokeAndWait(() -> ImageWindow.show(image, file.getName()));
        Processor processor = new Processor(image, size);
        if (mode.equals("S")) {
            processor.runSingle();
            System.out.println("Done with 1 thread.");
        } else {
            int threads = Runtime.getRuntime().availableProcessors();
            processor.runMulti(threads);
            System.out.println("Done with " + threads + " threads.");
        }
    }

    private static int parseSize(String text) {
        int size;
        try {
            size = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            size = 0;
        }
        if (size <= 0) {
            fail("Square size must be a positive integer.");
        }
        return size;
    }

    private static BufferedImage loadImage(File file) throws IOException {
        BufferedImage source = ImageIO.read(file);
        if (source == null) {
            fail("Not a supported image: " + file.getName());
        }
        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return image;
    }

    private static void fail(String message) {
        System.err.println(message);
        System.err.println(USAGE);
        System.exit(1);
    }
}