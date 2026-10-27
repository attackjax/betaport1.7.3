package dev.b173.mixin.content;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.b173.rules.BetaBlockTicks;
import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class BetaWeatherMixin {
    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void b173$betaWeatherTry(LevelChunk chunk, int speed, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (BetaRules.betaWorld(self) && self.getRandom().nextInt(16) == 0) {
            ChunkPos at = chunk.getPos();
            BetaBlockTicks.weather(self, self.getBlockRandomPos(at.getMinBlockX(), 0, at.getMinBlockZ(), 15));
        }
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0))
    private int b173$skipOwnTries(RandomSource random, int bound, Operation<Integer> original) {
        if (bound == 48 && BetaRules.betaWorld((ServerLevel) (Object) this)) {
            return 1;
        }
        return original.call(random, bound);
    }

    @Inject(method = "tickPrecipitation", at = @At("HEAD"), cancellable = true)
    private void b173$betaWeather(BlockPos pos, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (BetaRules.betaWorld(self)) {
            ci.cancel();
            BetaBlockTicks.weather(self, pos);
        }
    }
}
