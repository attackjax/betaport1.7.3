package dev.b173.rules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class BetaDrops {
    private BetaDrops() {
    }

    public static boolean dropOnDeath(ServerLevel level, LivingEntity entity) {
        EntityType<?> type = entity.getType();
        if (!BetaMobs.isBeta(type)) {
            return false;
        }
        if (type == EntityType.ZOMBIE || type == EntityType.CHICKEN) {
            dropFew(level, entity, Items.FEATHER);
        } else if (type == EntityType.COW) {
            dropFew(level, entity, Items.LEATHER);
        } else if (type == EntityType.SPIDER) {
            dropFew(level, entity, Items.STRING);
        } else if (type == EntityType.CREEPER || type == EntityType.GHAST) {
            dropFew(level, entity, Items.GUNPOWDER);
        } else if (type == EntityType.ZOMBIFIED_PIGLIN) {
            dropFew(level, entity, Items.COOKED_PORKCHOP);
        } else if (type == EntityType.PIG) {
            dropFew(level, entity, entity.isOnFire() ? Items.COOKED_PORKCHOP : Items.PORKCHOP);
        } else if (type == EntityType.SLIME) {
            if (entity instanceof Slime slime && slime.getSize() == 1) {
                dropFew(level, entity, Items.SLIME_BALL);
            }
        } else if (type == EntityType.SKELETON) {
            dropFew(level, entity, Items.ARROW);
            dropFew(level, entity, Items.BONE);
        } else if (type == EntityType.SHEEP) {
            if (entity instanceof Sheep sheep && !sheep.isSheared()) {
                entity.spawnAtLocation(level, new ItemStack(wool(sheep.getColor())));
            }
        } else if (type == EntityType.SQUID) {
            int count = entity.getRandom().nextInt(3) + 1;
            for (int i = 0; i < count; i++) {
                entity.spawnAtLocation(level, new ItemStack(Items.INK_SAC));
            }
        }
        return true;
    }

    private static void dropFew(ServerLevel level, LivingEntity entity, ItemLike item) {
        int count = entity.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            entity.spawnAtLocation(level, new ItemStack(item));
        }
    }

    public static void shear(ServerLevel level, Sheep sheep) {
        RandomSource random = sheep.getRandom();
        int count = 2 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            ItemEntity item = sheep.spawnAtLocation(level, new ItemStack(wool(sheep.getColor())), 1.0F);
            if (item != null) {
                item.setDeltaMovement(item.getDeltaMovement().add(
                        (random.nextFloat() - random.nextFloat()) * 0.1F,
                        random.nextFloat() * 0.05F,
                        (random.nextFloat() - random.nextFloat()) * 0.1F));
            }
        }
        sheep.setSheared(true);
    }

    private static Item wool(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_WOOL;
            case ORANGE -> Items.ORANGE_WOOL;
            case MAGENTA -> Items.MAGENTA_WOOL;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_WOOL;
            case YELLOW -> Items.YELLOW_WOOL;
            case LIME -> Items.LIME_WOOL;
            case PINK -> Items.PINK_WOOL;
            case GRAY -> Items.GRAY_WOOL;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_WOOL;
            case CYAN -> Items.CYAN_WOOL;
            case PURPLE -> Items.PURPLE_WOOL;
            case BLUE -> Items.BLUE_WOOL;
            case BROWN -> Items.BROWN_WOOL;
            case GREEN -> Items.GREEN_WOOL;
            case RED -> Items.RED_WOOL;
            case BLACK -> Items.BLACK_WOOL;
        };
    }
}
