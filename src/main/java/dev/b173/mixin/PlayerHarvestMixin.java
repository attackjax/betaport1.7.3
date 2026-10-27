package dev.b173.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaBlockDrops;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
abstract class PlayerHarvestMixin {
    @ModifyReturnValue(method = "hasCorrectToolForDrops", at = @At("RETURN"))
    private boolean b173$betaHarvest(boolean harvests, BlockState state) {
        return BetaBlockDrops.harvests((Player) (Object) this, state, harvests);
    }
}
