package dev.b173.core;

import dev.b173.core.pack.BetaPackBuilder;
import dev.b173.core.pack.BetaSounds;
import dev.b173.core.pack.Png;
import dev.b173.core.pack.Raster;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class TexturePackCheck {
    private TexturePackCheck() {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args[1]);
        long start = System.nanoTime();
        BetaPackBuilder.Result result;
        BetaSounds.Resources sounds = args.length > 2 ? folder(Path.of(args[2])) : null;
        try (ZipFile jar = new ZipFile(args[0])) {
            result = BetaPackBuilder.build(path -> {
                ZipEntry entry = jar.getEntry(path);
                if (entry == null) {
                    return null;
                }
                try (InputStream in = jar.getInputStream(entry)) {
                    return in.readAllBytes();
                }
            }, sounds, (path, data) -> {
                Path file = out.resolve(path);
                Files.createDirectories(file.getParent());
                Files.write(file, data);
            });
        }
        long built = System.nanoTime();
        int images = 0;
        long pixels = 0;
        try (var walk = Files.walk(out)) {
            for (Path file : (Iterable<Path>) walk.filter(p -> p.toString().endsWith(".png"))::iterator) {
                Raster image = Png.read(Files.readAllBytes(file));
                images++;
                pixels += (long) image.width * image.height;
            }
        }
        System.out.printf("wrote %d files in %.0f ms; re-read %d images (%d pixels) in %.0f ms%n",
                result.files(), (built - start) / 1e6, images, pixels, (System.nanoTime() - built) / 1e6);
        result.missing().forEach(m -> System.out.println("  not in the jar: " + m));
        if (result.sounds() != null) {
            System.out.printf("sounds: %d events, %d files; %d missing%n", result.sounds().events(),
                    result.sounds().files(), result.sounds().missing().size());
        }
    }

    private static BetaSounds.Resources folder(Path root) throws IOException {
        List<String> names;
        try (Stream<Path> walk = Files.walk(root)) {
            names = walk.filter(Files::isRegularFile).map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(BetaSounds::wanted).sorted().toList();
        }
        return new BetaSounds.Resources() {
            @Override
            public List<String> list() {
                return names;
            }

            @Override
            public byte[] read(String path) throws IOException {
                return names.contains(path) ? Files.readAllBytes(root.resolve(path)) : null;
            }
        };
    }
}
