package dev.b173.mixin.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAiAccessor {
    @Accessor("goalSelector")
    GoalSelector b173$goalSelector();

    @Accessor("targetSelector")
    GoalSelector b173$targetSelector();

    @Accessor("moveControl")
    void b173$setMoveControl(MoveControl control);

    @Accessor("lookControl")
    void b173$setLookControl(LookControl control);

    @Accessor("jumpControl")
    void b173$setJumpControl(JumpControl control);
}
