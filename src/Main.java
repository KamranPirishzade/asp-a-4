import java.io.File;

public class Main {
    private static final String USAGE = "Usage: java -cp out Main <image.jpg> <square size> <S|M>";

    public static void main(String[] args) {
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

        System.out.println("File: " + file.getName() + ", square size: " + size + ", mode: " + mode);
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

    private static void fail(String message) {
        System.err.println(message);
        System.err.println(USAGE);
        System.exit(1);
    }
}