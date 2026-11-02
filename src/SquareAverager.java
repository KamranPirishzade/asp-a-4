import java.util.Arrays;

public final class SquareAverager {
    private SquareAverager() {
    }

    public static int average(int[] pixels, int imageWidth, int x, int y, int width, int height) {
        long red = 0;
        long green = 0;
        long blue = 0;

        for (int row = y; row < y + height; row++) {
            for (int col = x; col < x + width; col++) {
                int rgb = pixels[row * imageWidth + col];
                red += (rgb >> 16) & 0xFF;
                green += (rgb >> 8) & 0xFF;
                blue += rgb & 0xFF;
            }
        }

        long count = (long) width * height;
        int color = (int) (red / count) << 16 | (int) (green / count) << 8 | (int) (blue / count);

        for (int row = y; row < y + height; row++) {
            int start = row * imageWidth + x;
            Arrays.fill(pixels, start, start + width, color);
        }
        return color;
    }
}