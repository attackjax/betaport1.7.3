package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.content.BetaDigging;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class BlockHardnessMixin {
    @ModifyReturnValue(method = "getDestroySpeed", at = @At("RETURN"))
    private float b173$betaHardness(float modern) {
        return BetaDigging.hardness((BlockState) (Object) this, modern);
    }
}
