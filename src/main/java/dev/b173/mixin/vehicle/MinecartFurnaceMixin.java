package dev.b173.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.b173.rules.BetaMinecart;
import dev.b173.rules.BetaRules;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean b173$betaFuel(boolean client) {
        return client || BetaRules.betaWorld(((MinecartFurnace) (Object) this).level());
    }

    @Inject(method = "addFuel", at = @At("HEAD"), cancellable = true)
    private void b173$betaCoal(Vec3 playerPos, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        MinecartFurnace self = (MinecartFurnace) (Object) this;
        if (BetaRules.betaWorld(self.level())) {
            info.setReturnValue(BetaMinecart.fuel(self, playerPos, stack));
        }
    }

    @ModifyReturnValue(method = "getDefaultDisplayBlockState", at = @At("RETURN"))
    private BlockState b173$betaFurnace(BlockState modern) {
        MinecartFurnace self = (MinecartFurnace) (Object) this;
        if (self.level() == null || !BetaRules.betaWorld(self.level()) || !modern.hasProperty(FurnaceBlock.FACING)) {
            return modern;
        }
        return modern.setValue(FurnaceBlock.FACING, Direction.SOUTH).setValue(FurnaceBlock.LIT, false);
    }
}
