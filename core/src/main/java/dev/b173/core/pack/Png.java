package dev.b173.core.pack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class Png {
    private static final byte[] SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

    private Png() {
    }

    public static Raster read(byte[] data) throws IOException {
        ByteBuffer in = ByteBuffer.wrap(data);
        for (byte b : SIGNATURE) {
            if (!in.hasRemaining() || in.get() != b) {
                throw new IOException("not a PNG");
            }
        }
        int width = 0;
        int height = 0;
        int depth = 0;
        int colourType = 0;
        byte[] palette = null;
        byte[] transparency = null;
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        while (in.remaining() >= 12) {
            int length = in.getInt();
            byte[] typeBytes = new byte[4];
            in.get(typeBytes);
            String type = new String(typeBytes, StandardCharsets.US_ASCII);
            if (length < 0 || length > in.remaining() - 4) {
                throw new IOException("truncated PNG chunk " + type);
            }
            byte[] body = new byte[length];
            in.get(body);
            in.getInt();
            switch (type) {
                case "IHDR" -> {
                    ByteBuffer header = ByteBuffer.wrap(body);
                    width = header.getInt();
                    height = header.getInt();
                    depth = header.get() & 255;
                    colourType = header.get() & 255;
                    int compression = header.get() & 255;
                    int filter = header.get() & 255;
                    int interlace = header.get() & 255;
                    if (compression != 0 || filter != 0 || interlace != 0) {
                        throw new IOException("unsupported PNG (interlaced or unknown method)");
                    }
                }
                case "PLTE" -> palette = body;
                case "tRNS" -> transparency = body;
                case "IDAT" -> compressed.write(body);
                case "IEND" -> {
                    return decode(width, height, depth, colourType, palette, transparency, compressed.toByteArray());
                }
                default -> {
                }
            }
        }
        throw new IOException("PNG without IEND");
    }

    private static Raster decode(int width, int height, int depth, int colourType, byte[] palette,
                                 byte[] transparency, byte[] compressed) throws IOException {
        int channels = switch (colourType) {
            case 0, 3 -> 1;
            case 2 -> 3;
            case 4 -> 2;
            case 6 -> 4;
            default -> throw new IOException("unknown PNG colour type " + colourType);
        };
        if (depth != 8 && !(colourType == 3 && (depth == 1 || depth == 2 || depth == 4))) {
            throw new IOException("unsupported PNG bit depth " + depth);
        }
        if (width <= 0 || height <= 0 || (long) width * height > 64L * 1024 * 1024) {
            throw new IOException("bad PNG size " + width + "x" + height);
        }
        int stride = (width * channels * depth + 7) / 8;
        int bytesPerPixel = Math.max(1, channels * depth / 8);
        byte[] raw = new byte[(stride + 1) * height];
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            int read = 0;
            while (read < raw.length) {
                int n = inflater.inflate(raw, read, raw.length - read);
                if (n == 0) {
                    if (inflater.finished() || inflater.needsInput() || inflater.needsDictionary()) {
                        throw new IOException("PNG image data too short");
                    }
                }
                read += n;
            }
        } catch (DataFormatException e) {
            throw new IOException("corrupt PNG image data", e);
        } finally {
            inflater.end();
        }

        Raster out = new Raster(width, height);
        byte[] previous = new byte[stride];
        byte[] line = new byte[stride];
        for (int y = 0; y < height; y++) {
            int filter = raw[y * (stride + 1)] & 255;
            System.arraycopy(raw, y * (stride + 1) + 1, line, 0, stride);
            unfilter(filter, line, previous, bytesPerPixel);
            for (int x = 0; x < width; x++) {
                out.argb[y * width + x] = pixel(line, x, colourType, depth, palette, transparency);
            }
            byte[] swap = previous;
            previous = line;
            line = swap;
        }
        return out;
    }

    private static void unfilter(int filter, byte[] line, byte[] previous, int bpp) throws IOException {
        for (int i = 0; i < line.length; i++) {
            int a = i >= bpp ? line[i - bpp] & 255 : 0;
            int b = previous[i] & 255;
            int c = i >= bpp ? previous[i - bpp] & 255 : 0;
            int x = line[i] & 255;
            line[i] = (byte) switch (filter) {
                case 0 -> x;
                case 1 -> x + a;
                case 2 -> x + b;
                case 3 -> x + (a + b >> 1);
                case 4 -> x + paeth(a, b, c);
                default -> throw new IOException("unknown PNG filter " + filter);
            };
        }
    }

    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        return pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
    }

    private static int pixel(byte[] line, int x, int colourType, int depth, byte[] palette, byte[] transparency)
            throws IOException {
        switch (colourType) {
            case 6 -> {
                int i = x * 4;
                return (line[i + 3] & 255) << 24 | (line[i] & 255) << 16 | (line[i + 1] & 255) << 8 | line[i + 2] & 255;
            }
            case 2 -> {
                int i = x * 3;
                return 0xFF000000 | (line[i] & 255) << 16 | (line[i + 1] & 255) << 8 | line[i + 2] & 255;
            }
            case 0 -> {
                int v = line[x] & 255;
                return 0xFF000000 | v << 16 | v << 8 | v;
            }
            case 4 -> {
                int v = line[x * 2] & 255;
                return (line[x * 2 + 1] & 255) << 24 | v << 16 | v << 8 | v;
            }
            default -> {
                int index;
                if (depth == 8) {
                    index = line[x] & 255;
                } else {
                    int perByte = 8 / depth;
                    int shift = 8 - depth * (x % perByte + 1);
                    index = (line[x / perByte] & 255) >> shift & (1 << depth) - 1;
                }
                if (palette == null || index * 3 + 2 >= palette.length) {
                    throw new IOException("PNG palette index out of range");
                }
                int alpha = transparency != null && index < transparency.length ? transparency[index] & 255 : 255;
                return alpha << 24 | (palette[index * 3] & 255) << 16 | (palette[index * 3 + 1] & 255) << 8
                        | palette[index * 3 + 2] & 255;
            }
        }
    }

    public static byte[] write(Raster image) {
        int stride = image.width * 4;
        byte[] raw = new byte[(stride + 1) * image.height];
        byte[] previous = new byte[stride];
        byte[] line = new byte[stride];
        byte[] best = new byte[stride];
        byte[] trial = new byte[stride];
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int p = image.argb[y * image.width + x];
                line[x * 4] = (byte) (p >> 16);
                line[x * 4 + 1] = (byte) (p >> 8);
                line[x * 4 + 2] = (byte) p;
                line[x * 4 + 3] = (byte) (p >>> 24);
            }
            int bestFilter = 0;
            long bestScore = Long.MAX_VALUE;
            for (int filter = 0; filter <= 4; filter++) {
                long score = 0;
                for (int i = 0; i < stride; i++) {
                    int a = i >= 4 ? line[i - 4] & 255 : 0;
                    int b = previous[i] & 255;
                    int c = i >= 4 ? previous[i - 4] & 255 : 0;
                    int x = line[i] & 255;
                    int v = switch (filter) {
                        case 0 -> x;
                        case 1 -> x - a;
                        case 2 -> x - b;
                        case 3 -> x - (a + b >> 1);
                        default -> x - paeth(a, b, c);
                    };
                    trial[i] = (byte) v;
                    score += Math.abs((byte) v);
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestFilter = filter;
                    byte[] swap = best;
                    best = trial;
                    trial = swap;
                }
            }
            raw[y * (stride + 1)] = (byte) bestFilter;
            System.arraycopy(best, 0, raw, y * (stride + 1) + 1, stride);
            byte[] swap = previous;
            previous = line;
            line = swap;
        }

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        ByteArrayOutputStream idat = new ByteArrayOutputStream();
        try {
            deflater.setInput(raw);
            deflater.finish();
            byte[] buffer = new byte[65536];
            while (!deflater.finished()) {
                int n = deflater.deflate(buffer);
                idat.write(buffer, 0, n);
            }
        } finally {
            deflater.end();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(idat.size() + 64);
        out.writeBytes(SIGNATURE);
        ByteBuffer header = ByteBuffer.allocate(13);
        header.putInt(image.width).putInt(image.height).put((byte) 8).put((byte) 6).put((byte) 0).put((byte) 0).put((byte) 0);
        chunk(out, "IHDR", header.array());
        chunk(out, "IDAT", idat.toByteArray());
        chunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static void chunk(ByteArrayOutputStream out, String type, byte[] body) {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(body);
        ByteBuffer length = ByteBuffer.allocate(4).putInt(body.length);
        out.writeBytes(length.array());
        out.writeBytes(typeBytes);
        out.writeBytes(body);
        out.writeBytes(ByteBuffer.allocate(4).putInt((int) crc.getValue()).array());
    }
}
