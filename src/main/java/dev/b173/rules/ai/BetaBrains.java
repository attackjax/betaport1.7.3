package dev.b173.rules.ai;

import dev.b173.mixin.ai.MobAiAccessor;
import dev.b173.rules.BetaMobs;
import dev.b173.rules.BetaRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

public final class BetaBrains {
    private BetaBrains() {
    }

    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register(BetaBrains::onLoad);
    }

    public static BetaBrain brainOf(Entity entity) {
        return entity instanceof BetaBrainHolder holder ? holder.b173$brain() : null;
    }

    private static void onLoad(Entity entity, ServerLevel level) {
        if (BetaRules.betaSizes() && BetaMobs.hasBetaSize(entity.getType())) {
            entity.refreshDimensions();
        }
        if (!(entity instanceof Mob mob) || mob.isNoAi() || brainOf(mob) != null
                || !BetaRules.active(level.getServer())) {
            return;
        }
        BetaBrain brain = create(mob);
        if (brain == null) {
            return;
        }
        MobAiAccessor access = (MobAiAccessor) mob;
        access.b173$goalSelector().removeAllGoals(goal -> true);
        access.b173$targetSelector().removeAllGoals(goal -> true);
        access.b173$setMoveControl(new BetaBrain.StillMoveControl(mob));
        access.b173$setLookControl(new BetaBrain.StillLookControl(mob));
        access.b173$setJumpControl(new BetaBrain.StillJumpControl(mob));
        mob.getNavigation().stop();
        mob.setTarget(null);
        mob.setCanPickUpLoot(false);
        access.b173$goalSelector().addGoal(0, brain);
        ((BetaBrainHolder) mob).b173$setBrain(brain);
    }

    private static BetaBrain create(Mob mob) {
        EntityType<?> type = mob.getType();
        if (type == EntityType.ZOMBIE) {
            return new BetaMonsterBrain(mob, 0.5F);
        }
        if (type == EntityType.GIANT) {
            return new BetaMonsterBrain.Giant(mob);
        }
        if (type == EntityType.SKELETON) {
            return new BetaSkeletonBrain(mob);
        }
        if (type == EntityType.SPIDER) {
            return new BetaSpiderBrain(mob);
        }
        if (type == EntityType.CREEPER && mob instanceof Creeper creeper) {
            return new BetaCreeperBrain(creeper);
        }
        if (type == EntityType.PIG || type == EntityType.COW || type == EntityType.SHEEP
                || type == EntityType.CHICKEN) {
            return new BetaAnimalBrain(mob);
        }
        if (type == EntityType.WOLF && mob instanceof Wolf wolf) {
            return new BetaWolfBrain(wolf);
        }
        if (type == EntityType.ZOMBIFIED_PIGLIN && mob instanceof ZombifiedPiglin pigman) {
            return new BetaPigmanBrain(pigman);
        }
        if (type == EntityType.SQUID && mob instanceof Squid squid) {
            return new BetaSquidBrain(squid);
        }
        if (type == EntityType.SLIME && mob instanceof Slime slime) {
            return new BetaSlimeBrain(slime);
        }
        if (type == EntityType.GHAST && mob instanceof Ghast ghast) {
            return new BetaGhastBrain(ghast);
        }
        return null;
    }
}
