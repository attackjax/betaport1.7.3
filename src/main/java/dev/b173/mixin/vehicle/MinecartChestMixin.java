package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaMinecart;
import dev.b173.rules.BetaRules;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecartChest.class)
abstract class MinecartChestMixin {
    @ModifyReturnValue(method = "getDefaultDisplayOffset", at = @At("RETURN"))
    private int b173$betaOffset(int modern) {
        MinecartChest self = (MinecartChest) (Object) this;
        return self.level() != null && BetaRules.betaWorld(self.level()) ? BetaMinecart.DISPLAY_OFFSET : modern;
    }

    @ModifyReturnValue(method = "getDefaultDisplayBlockState", at = @At("RETURN"))
    private BlockState b173$betaFront(BlockState modern) {
        MinecartChest self = (MinecartChest) (Object) this;
        return self.level() != null && BetaRules.betaWorld(self.level()) && modern.hasProperty(ChestBlock.FACING)
                ? modern.setValue(ChestBlock.FACING, Direction.SOUTH) : modern;
    }
}
