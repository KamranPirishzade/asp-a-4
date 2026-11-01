import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.List;

public class Processor {
    private static final long ANIMATION_NANOS = 10_000_000_000L;
    private static final long MAX_PAUSE_NANOS = 100_000_000L;

    private final int[] pixels;
    private final int width;
    private final int height;
    private final int size;
    private final int rows;
    private final long pauseNanos;

    public Processor(BufferedImage image, int size) {
        this.pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.size = size;
        this.rows = (height + size - 1) / size;
        int columns = (width + size - 1) / size;
        this.pauseNanos = Boolean.getBoolean("noDelay")? 0 : Math.min(ANIMATION_NANOS / ((long) rows * columns), MAX_PAUSE_NANOS);
    }

    public void runSingle() {
        processRows(0, rows);
    }

    public void runMulti(int threadCount) throws InterruptedException {
        int threads = Math.min(threadCount, rows);
        List<Thread> workers = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int firstRow = i * rows / threads;
            int lastRow = (i + 1) * rows / threads;
            Thread worker = new Thread(() -> processRows(firstRow, lastRow));
            workers.add(worker);
            worker.start();
        }

        for (Thread worker : workers) {
            worker.join();
        }
    }

    private void processRows(int firstRow, int lastRow) {
        long next = System.nanoTime();
        for (int row = firstRow; row < lastRow; row++) {
            int y = row * size;
            int squareHeight = Math.min(size, height - y);
            for (int x = 0; x < width; x += size) {
                SquareAverager.average(pixels, width, x, y, Math.min(size, width - x), squareHeight);
                next += pauseNanos;
                pauseUntil(next);
            }
        }
    }

    private static void pauseUntil(long time) {
        long millis = (time - System.nanoTime()) / 1_000_000;
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}