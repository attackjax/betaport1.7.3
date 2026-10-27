package dev.b173.mixin;

import dev.b173.rules.BetaRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Zombie.class, AbstractSkeleton.class, Spider.class, Pig.class, AbstractCow.class, Sheep.class, Chicken.class,
        Wolf.class})
public abstract class MobBlockStepSoundMixin {
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void b173$blockStepSound(BlockPos pos, BlockState state, CallbackInfo callback) {
        Entity self = (Entity) (Object) this;
        if (BetaRules.betaWorld(self.level())) {
            SoundType sound = state.getSoundType();
            self.playSound(sound.getStepSound(), sound.getVolume() * 0.15F, sound.getPitch());
            callback.cancel();
        }
    }
}
