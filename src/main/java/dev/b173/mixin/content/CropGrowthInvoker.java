package dev.b173.mixin.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface CropGrowthInvoker {
    @Invoker("getGrowthSpeed")
    static float b173$growthSpeed(Block block, BlockGetter level, BlockPos pos) {
        throw new AssertionError();
    }
}
