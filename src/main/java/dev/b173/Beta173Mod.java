package dev.b173;

import dev.b173.net.BetaNetworking;
import dev.b173.rules.BetaRules;
import dev.b173.world.BetaBiomeSource;
import dev.b173.world.BetaChunkGenerator;
import dev.b173.world.BetaNetherGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Beta173Mod implements ModInitializer {
    public static final String NAMESPACE = "b173";
    public static final Logger LOGGER = LoggerFactory.getLogger("Betaport");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("beta"), BetaChunkGenerator.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("beta_nether"), BetaNetherGenerator.CODEC);
        Registry.register(BuiltInRegistries.BIOME_SOURCE, id("beta"), BetaBiomeSource.CODEC);
        BetaNetworking.init();
        BetaRules.init();
        dev.b173.content.BetaConversion.init();
    }
}
