package dev.b173.mixin.ai;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.b173.rules.BetaRules;
import dev.b173.rules.BetaSleepSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {
    @Shadow
    @Final
    private List<ServerPlayer> players;

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"))
    private boolean b173$monsterWakes(boolean enough) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (enough && level.dimension() == Level.OVERWORLD && level.isSpawningMonsters()
                && BetaRules.active(level.getServer())) {
            return !BetaSleepSpawner.wakeWithMonster(level, players);
        }
        return enough;
    }
}
