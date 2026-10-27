package dev.b173.rules;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.wolf.Wolf;

import java.util.Map;
import java.util.Set;

public final class BetaMobs {
    private static final Set<EntityType<?>> BETA_MOBS = Set.of(EntityType.ZOMBIE, EntityType.CHICKEN,
            EntityType.COW, EntityType.SPIDER, EntityType.CREEPER, EntityType.GHAST, EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PIG, EntityType.SLIME, EntityType.SKELETON, EntityType.SHEEP, EntityType.SQUID,
            EntityType.WOLF, EntityType.GIANT);

    private static final Size BOAT = new Size(1.5F, 0.6F, 0.6F);

    private static final Map<EntityType<?>, Size> SIZES = Map.ofEntries(
            Map.entry(EntityType.ZOMBIE, Size.of(0.6F, 1.8F)),
            Map.entry(EntityType.SKELETON, Size.of(0.6F, 1.8F)),
            Map.entry(EntityType.CREEPER, Size.of(0.6F, 1.8F)),
            Map.entry(EntityType.ZOMBIFIED_PIGLIN, Size.of(0.6F, 1.8F)),
            Map.entry(EntityType.SPIDER, Size.of(1.4F, 0.9F)),
            Map.entry(EntityType.PIG, Size.of(0.9F, 0.9F)),
            Map.entry(EntityType.COW, Size.of(0.9F, 1.3F)),
            Map.entry(EntityType.SHEEP, Size.of(0.9F, 1.3F)),
            Map.entry(EntityType.CHICKEN, Size.of(0.3F, 0.4F)),
            Map.entry(EntityType.WOLF, new Size(0.8F, 0.8F, 0.8F * 0.8F)),
            Map.entry(EntityType.SQUID, Size.of(0.95F, 0.95F)),
            Map.entry(EntityType.SLIME, Size.of(0.6F, 0.6F)),
            Map.entry(EntityType.GHAST, Size.of(4.0F, 4.0F)),
            Map.entry(EntityType.GIANT, Size.of(0.6F * 6.0F, 1.8F * 6.0F)),
            Map.entry(EntityType.OAK_BOAT, BOAT),
            Map.entry(EntityType.SPRUCE_BOAT, BOAT),
            Map.entry(EntityType.BIRCH_BOAT, BOAT),
            Map.entry(EntityType.JUNGLE_BOAT, BOAT),
            Map.entry(EntityType.ACACIA_BOAT, BOAT),
            Map.entry(EntityType.CHERRY_BOAT, BOAT),
            Map.entry(EntityType.DARK_OAK_BOAT, BOAT),
            Map.entry(EntityType.PALE_OAK_BOAT, BOAT),
            Map.entry(EntityType.MANGROVE_BOAT, BOAT));

    private record Size(float width, float height, float eyeHeight) {
        static Size of(float width, float height) {
            return new Size(width, height, height * 0.85F);
        }
    }

    private BetaMobs() {
    }

    public static boolean hasBetaSize(EntityType<?> type) {
        return SIZES.containsKey(type);
    }

    public static EntityDimensions betaDimensions(EntityType<?> type, EntityDimensions modern) {
        Size size = SIZES.get(type);
        if (size == null) {
            return modern;
        }
        return new EntityDimensions(size.width(), size.height(), size.eyeHeight(), modern.attachments(),
                modern.fixed());
    }

    public static boolean isBeta(EntityType<?> type) {
        return BETA_MOBS.contains(type);
    }

    public static float maxHealth(LivingEntity entity, float modern) {
        EntityType<?> type = entity.getType();
        if (type == EntityType.SPIDER) {
            return 20.0F;
        }
        if (type == EntityType.SHEEP) {
            return 10.0F;
        }
        if (type == EntityType.WOLF) {
            return entity instanceof Wolf wolf && wolf.isTame() ? 20.0F : 8.0F;
        }
        if (type == EntityType.GIANT) {
            return 200.0F;
        }
        return modern;
    }

    public static float attackDamage(Mob mob, float modern) {
        EntityType<?> type = mob.getType();
        if (type == EntityType.ZOMBIE || type == EntityType.ZOMBIFIED_PIGLIN) {
            return 5.0F;
        }
        if (type == EntityType.SPIDER) {
            return 2.0F;
        }
        if (type == EntityType.WOLF) {
            return mob instanceof Wolf wolf && wolf.isTame() ? 4.0F : 2.0F;
        }
        if (type == EntityType.GIANT) {
            return 50.0F;
        }
        return modern;
    }

    public static int maxPerChunk(EntityType<?> type) {
        if (type == EntityType.WOLF) {
            return 8;
        }
        if (type == EntityType.GHAST) {
            return 1;
        }
        return 4;
    }

    public static boolean despawnsLikeBeta(Mob mob) {
        EntityType<?> type = mob.getType();
        if (type == EntityType.WOLF) {
            return !(mob instanceof Wolf wolf && wolf.isTame());
        }
        return type == EntityType.PIG || type == EntityType.COW || type == EntityType.SHEEP
                || type == EntityType.CHICKEN;
    }
}
