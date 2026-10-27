package dev.b173.net;

import dev.b173.Beta173Mod;
import dev.b173.world.BetaChunkGenerator;
import dev.b173.world.BetaNetherGenerator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record BetaWorldPayload(long seed, List<String> dimensions, List<String> nethers)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BetaWorldPayload> TYPE =
            new CustomPacketPayload.Type<>(Beta173Mod.id("world"));
    public static final StreamCodec<FriendlyByteBuf, BetaWorldPayload> CODEC =
            CustomPacketPayload.codec(BetaWorldPayload::write, BetaWorldPayload::new);

    private BetaWorldPayload(FriendlyByteBuf buf) {
        this(buf.readLong(), readDimensions(buf), readDimensions(buf));
    }

    private static List<String> readDimensions(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<String> dimensions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            dimensions.add(buf.readUtf());
        }
        return dimensions;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeLong(seed);
        writeDimensions(buf, dimensions);
        writeDimensions(buf, nethers);
    }

    private static void writeDimensions(FriendlyByteBuf buf, List<String> list) {
        buf.writeVarInt(list.size());
        for (String dimension : list) {
            buf.writeUtf(dimension);
        }
    }

    public static @Nullable BetaWorldPayload of(MinecraftServer server) {
        List<String> dimensions = new ArrayList<>();
        List<String> nethers = new ArrayList<>();
        long seed = 0L;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getChunkSource().getGenerator() instanceof BetaChunkGenerator generator) {
                dimensions.add(level.dimension().identifier().toString());
                seed = generator.seed();
            } else if (level.getChunkSource().getGenerator() instanceof BetaNetherGenerator) {
                nethers.add(level.dimension().identifier().toString());
            }
        }
        return dimensions.isEmpty() ? null : new BetaWorldPayload(seed, List.copyOf(dimensions), List.copyOf(nethers));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
