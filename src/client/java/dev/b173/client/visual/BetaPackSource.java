package dev.b173.client.visual;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class BetaPackSource implements RepositorySource {
    public static final BetaPackSource INSTANCE = new BetaPackSource();
    public static final String PACK_ID = "b173/beta";

    private BetaPackSource() {
    }

    @Override
    public void loadPacks(Consumer<Pack> packs) {
        Path folder = BetaPack.prepare();
        if (folder == null) {
            return;
        }
        PackLocationInfo location = new PackLocationInfo(PACK_ID, Component.literal("Beta 1.7.3"),
                PackSource.BUILT_IN, Optional.empty());
        Pack pack = Pack.readMetaAndCreate(location, new PathPackResources.PathResourcesSupplier(folder),
                PackType.CLIENT_RESOURCES, new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (pack != null) {
            packs.accept(pack);
        }
    }
}
