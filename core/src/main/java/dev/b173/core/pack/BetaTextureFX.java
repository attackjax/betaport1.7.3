package dev.b173.core.pack;

import dev.b173.core.BetaMath;

import java.util.Random;
import java.util.function.DoubleSupplier;

public final class BetaTextureFX {
    private static final float PI = 3.1415927F;

    private BetaTextureFX() {
    }

    public abstract static class Effect {
        public final byte[] pixels = new byte[1024];
        protected final DoubleSupplier random;

        protected Effect(DoubleSupplier random) {
            this.random = random;
        }

        public final void tick() {
            step();
            paint(field(), scroll(), pixels);
        }

        protected abstract void step();

        protected abstract float[] field();

        protected int scroll() {
            return 0;
        }

        protected abstract void paint(float[] field, int scroll, byte[] out);

        public int repeat() {
            return 1;
        }
    }

    public static final class Lava extends Effect {
        private float[] g = new float[256];
        private float[] h = new float[256];
        private final float[] i = new float[256];
        private final float[] j = new float[256];
        private final boolean flowing;
        private int ticks;

        public Lava(boolean flowing, DoubleSupplier random) {
            super(random);
            this.flowing = flowing;
        }

        @Override
        protected void step() {
            if (flowing) {
                ticks++;
            }
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 16; b++) {
                    float sum = 0.0F;
                    int dx = (int) (BetaMath.sin((float) b * PI * 2.0F / 16.0F) * 1.2F);
                    int dy = (int) (BetaMath.sin((float) a * PI * 2.0F / 16.0F) * 1.2F);
                    for (int x = a - 1; x <= a + 1; x++) {
                        for (int y = b - 1; y <= b + 1; y++) {
                            sum += g[(x + dx & 15) + (y + dy & 15) * 16];
                        }
                    }
                    int at = a + b * 16;
                    h[at] = sum / 10.0F
                            + (i[(a & 15) + (b & 15) * 16] + i[(a + 1 & 15) + (b & 15) * 16]
                            + i[(a + 1 & 15) + (b + 1 & 15) * 16] + i[(a & 15) + (b + 1 & 15) * 16]) / 4.0F * 0.8F;
                    i[at] += j[at] * 0.01F;
                    if (i[at] < 0.0F) {
                        i[at] = 0.0F;
                    }
                    j[at] -= 0.06F;
                    if (random.getAsDouble() < 0.005D) {
                        j[at] = 1.5F;
                    }
                }
            }
            float[] swap = h;
            h = g;
            g = swap;
        }

        @Override
        protected float[] field() {
            return g;
        }

        @Override
        protected int scroll() {
            return flowing ? ticks / 3 : 0;
        }

        @Override
        protected void paint(float[] field, int scroll, byte[] out) {
            for (int at = 0; at < 256; at++) {
                float f = field[at - scroll * 16 & 255] * 2.0F;
                if (f > 1.0F) {
                    f = 1.0F;
                }
                if (f < 0.0F) {
                    f = 0.0F;
                }
                out[at * 4] = (byte) (int) (f * 100.0F + 155.0F);
                out[at * 4 + 1] = (byte) (int) (f * f * 255.0F);
                out[at * 4 + 2] = (byte) (int) (f * f * f * f * 128.0F);
                out[at * 4 + 3] = (byte) 255;
            }
        }

        @Override
        public int repeat() {
            return flowing ? 2 : 1;
        }
    }

    public static final class Water extends Effect {
        private float[] g = new float[256];
        private float[] h = new float[256];
        private final float[] i = new float[256];
        private final float[] j = new float[256];
        private final boolean flowing;
        private int ticks;

        public Water(boolean flowing, DoubleSupplier random) {
            super(random);
            this.flowing = flowing;
        }

        @Override
        protected void step() {
            ticks++;
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 16; b++) {
                    float sum = 0.0F;
                    if (flowing) {
                        for (int y = b - 2; y <= b; y++) {
                            sum += g[(a & 15) + (y & 15) * 16];
                        }
                        h[a + b * 16] = sum / 3.2F + i[a + b * 16] * 0.8F;
                    } else {
                        for (int x = a - 1; x <= a + 1; x++) {
                            sum += g[(x & 15) + (b & 15) * 16];
                        }
                        h[a + b * 16] = sum / 3.3F + i[a + b * 16] * 0.8F;
                    }
                }
            }
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 16; b++) {
                    int at = a + b * 16;
                    i[at] += j[at] * 0.05F;
                    if (i[at] < 0.0F) {
                        i[at] = 0.0F;
                    }
                    if (flowing) {
                        j[at] -= 0.3F;
                        if (random.getAsDouble() < 0.2D) {
                            j[at] = 0.5F;
                        }
                    } else {
                        j[at] -= 0.1F;
                        if (random.getAsDouble() < 0.05D) {
                            j[at] = 0.5F;
                        }
                    }
                }
            }
            float[] swap = h;
            h = g;
            g = swap;
        }

        @Override
        protected float[] field() {
            return g;
        }

        @Override
        protected int scroll() {
            return flowing ? ticks : 0;
        }

        @Override
        protected void paint(float[] field, int scroll, byte[] out) {
            for (int at = 0; at < 256; at++) {
                float f = field[at - scroll * 16 & 255];
                if (f > 1.0F) {
                    f = 1.0F;
                }
                if (f < 0.0F) {
                    f = 0.0F;
                }
                float f2 = f * f;
                out[at * 4] = (byte) (int) (32.0F + f2 * 32.0F);
                out[at * 4 + 1] = (byte) (int) (50.0F + f2 * 64.0F);
                out[at * 4 + 2] = (byte) 255;
                out[at * 4 + 3] = (byte) (int) (146.0F + f2 * 50.0F);
            }
        }

        @Override
        public int repeat() {
            return flowing ? 2 : 1;
        }
    }

    public static final class Fire extends Effect {
        private float[] g = new float[320];
        private float[] h = new float[320];

        public Fire(DoubleSupplier random) {
            super(random);
        }

        @Override
        protected void step() {
            for (int a = 0; a < 16; a++) {
                for (int b = 0; b < 20; b++) {
                    int count = 18;
                    float f = g[a + (b + 1) % 20 * 16] * (float) count;
                    for (int x = a - 1; x <= a + 1; x++) {
                        for (int y = b; y <= b + 1; y++) {
                            if (x >= 0 && y >= 0 && x < 16 && y < 20) {
                                f += g[x + y * 16];
                            }
                            count++;
                        }
                    }
                    h[a + b * 16] = f / ((float) count * 1.06F);
                    if (b >= 19) {
                        h[a + b * 16] = (float) (random.getAsDouble() * random.getAsDouble() * random.getAsDouble() * 4.0D
                                + random.getAsDouble() * (double) 0.1F + (double) 0.2F);
                    }
                }
            }
            float[] swap = h;
            h = g;
            g = swap;
        }

        @Override
        protected float[] field() {
            return g;
        }

        @Override
        protected void paint(float[] field, int scroll, byte[] out) {
            for (int at = 0; at < 256; at++) {
                float f = field[at] * 1.8F;
                if (f > 1.0F) {
                    f = 1.0F;
                }
                if (f < 0.0F) {
                    f = 0.0F;
                }
                int r = (int) (f * 155.0F + 100.0F);
                int g2 = (int) (f * f * 255.0F);
                int b = (int) (f * f * f * f * f * f * f * f * f * f * 255.0F);
                out[at * 4] = (byte) r;
                out[at * 4 + 1] = (byte) g2;
                out[at * 4 + 2] = (byte) b;
                out[at * 4 + 3] = (byte) (f < 0.5F ? 0 : 255);
            }
        }
    }

    public static final class Portal extends Effect {
        private final byte[][] frames = new byte[32][1024];
        private int ticks;

        public Portal() {
            super(() -> 0.0D);
            Random random = new Random(100L);
            for (int frame = 0; frame < 32; frame++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        float f = 0.0F;
                        for (int s = 0; s < 2; s++) {
                            float ox = (float) (s * 8);
                            float oy = (float) (s * 8);
                            float u = ((float) x - ox) / 16.0F * 2.0F;
                            float v = ((float) y - oy) / 16.0F * 2.0F;
                            if (u < -1.0F) {
                                u += 2.0F;
                            }
                            if (u >= 1.0F) {
                                u -= 2.0F;
                            }
                            if (v < -1.0F) {
                                v += 2.0F;
                            }
                            if (v >= 1.0F) {
                                v -= 2.0F;
                            }
                            float distance = u * u + v * v;
                            float angle = (float) StrictMath.atan2((double) v, (double) u)
                                    + ((float) frame / 32.0F * PI * 2.0F - distance * 10.0F + (float) (s * 2))
                                    * (float) (s * 2 - 1);
                            angle = (BetaMath.sin(angle) + 1.0F) / 2.0F;
                            angle = angle / (distance + 1.0F);
                            f += angle * 0.5F;
                        }
                        f += random.nextFloat() * 0.1F;
                        int at = (y * 16 + x) * 4;
                        frames[frame][at] = (byte) (int) (f * f * 200.0F + 55.0F);
                        frames[frame][at + 1] = (byte) (int) (f * f * f * f * 255.0F);
                        frames[frame][at + 2] = (byte) (int) (f * 100.0F + 155.0F);
                        frames[frame][at + 3] = (byte) (int) (f * 100.0F + 155.0F);
                    }
                }
            }
        }

        @Override
        protected void step() {
            ticks++;
        }

        @Override
        protected float[] field() {
            return null;
        }

        @Override
        protected void paint(float[] field, int scroll, byte[] out) {
            System.arraycopy(frames[ticks & 31], 0, out, 0, 1024);
        }

        public byte[] frame(int index) {
            return frames[index & 31];
        }
    }

    public static Raster[] loop(Effect effect, int warmUp, int length, int blend) {
        if (effect instanceof Portal portal) {
            Raster[] out = new Raster[32];
            for (int frame = 0; frame < 32; frame++) {
                out[frame] = Raster.fromRgba(portal.frame(frame), 16, 16);
            }
            return out;
        }
        if (blend < 0 || blend >= length) {
            throw new IllegalArgumentException("blend must be shorter than the loop");
        }
        for (int t = 0; t < warmUp; t++) {
            effect.tick();
        }
        int total = length + blend;
        float[][] fields = new float[total][];
        int[] scrolls = new int[total];
        for (int t = 0; t < total; t++) {
            effect.tick();
            fields[t] = effect.field().clone();
            scrolls[t] = effect.scroll();
        }
        Raster[] out = new Raster[length];
        byte[] rgba = new byte[1024];
        for (int frame = 0; frame < length; frame++) {
            int at = frame + blend;
            float[] field = fields[at];
            if (frame >= length - blend) {
                float weight = (float) (frame - (length - blend) + 1) / (float) (blend + 1);
                float[] earlier = fields[at - length];
                float[] mixed = new float[field.length];
                for (int k = 0; k < field.length; k++) {
                    mixed[k] = field[k] + (earlier[k] - field[k]) * weight;
                }
                field = mixed;
            }
            effect.paint(field, scrolls[at], rgba);
            Raster image = Raster.fromRgba(rgba, 16, 16);
            out[frame] = effect.repeat() == 1 ? image : image.tile(2, 2);
        }
        return out;
    }

    public static DoubleSupplier seeded(long seed) {
        Random random = new Random(seed);
        return random::nextDouble;
    }
}
