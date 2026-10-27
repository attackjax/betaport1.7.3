package dev.b173.core.pack;

public final class Raster {
    public final int width;
    public final int height;
    public final int[] argb;

    public Raster(int width, int height) {
        this(width, height, new int[width * height]);
    }

    public Raster(int width, int height, int[] argb) {
        if (width <= 0 || height <= 0 || argb.length != width * height) {
            throw new IllegalArgumentException("bad raster " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.argb = argb;
    }

    public int get(int x, int y) {
        return argb[y * width + x];
    }

    public void set(int x, int y, int value) {
        argb[y * width + x] = value;
    }

    public Raster crop(int x, int y, int w, int h) {
        if (x < 0 || y < 0 || x + w > width || y + h > height) {
            throw new IllegalArgumentException("crop " + x + "," + y + " " + w + "x" + h + " outside " + width + "x" + height);
        }
        Raster out = new Raster(w, h);
        for (int row = 0; row < h; row++) {
            System.arraycopy(argb, (y + row) * width + x, out.argb, row * w, w);
        }
        return out;
    }

    public Raster paste(Raster source, int x, int y) {
        for (int row = 0; row < source.height; row++) {
            System.arraycopy(source.argb, row * source.width, argb, (y + row) * width + x, source.width);
        }
        return this;
    }

    public Raster copy() {
        return new Raster(width, height, argb.clone());
    }

    public Raster rotateClockwise() {
        Raster out = new Raster(height, width);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.set(height - 1 - y, x, get(x, y));
            }
        }
        return out;
    }

    public static Raster strip(Raster[] frames) {
        Raster first = frames[0];
        Raster out = new Raster(first.width, first.height * frames.length);
        for (int i = 0; i < frames.length; i++) {
            out.paste(frames[i], 0, i * first.height);
        }
        return out;
    }

    public Raster tile(int columns, int rows) {
        Raster out = new Raster(width * columns, height * rows);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                out.paste(this, c * width, r * height);
            }
        }
        return out;
    }

    public Raster multiply(int rgb) {
        int mr = rgb >> 16 & 255;
        int mg = rgb >> 8 & 255;
        int mb = rgb & 255;
        Raster out = new Raster(width, height);
        for (int i = 0; i < argb.length; i++) {
            int p = argb[i];
            int r = (p >> 16 & 255) * mr / 255;
            int g = (p >> 8 & 255) * mg / 255;
            int b = (p & 255) * mb / 255;
            out.argb[i] = p & 0xFF000000 | r << 16 | g << 8 | b;
        }
        return out;
    }

    public boolean isEmpty() {
        for (int p : argb) {
            if (p >>> 24 != 0) {
                return false;
            }
        }
        return true;
    }

    public static Raster fromRgba(byte[] rgba, int width, int height) {
        Raster out = new Raster(width, height);
        for (int i = 0; i < width * height; i++) {
            int r = rgba[i * 4] & 255;
            int g = rgba[i * 4 + 1] & 255;
            int b = rgba[i * 4 + 2] & 255;
            int a = rgba[i * 4 + 3] & 255;
            out.argb[i] = a << 24 | r << 16 | g << 8 | b;
        }
        return out;
    }
}
