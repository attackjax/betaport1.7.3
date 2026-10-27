package dev.b173.client.mixin.gui;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(WorldCreationUiState.class)
public interface WorldCreationUiStateAccessor {
    @Accessor("savesFolder")
    Path b173$savesFolder();

    @Accessor("targetFolder")
    void b173$setTargetFolder(String folder);
}
