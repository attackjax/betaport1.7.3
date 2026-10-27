package dev.b173.client.gui;

import dev.b173.client.mixin.gui.AbstractContainerScreenAccessor;
import dev.b173.client.model.BetaModels;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class BetaScreens {
    private static final int PREVIEW_X = 51;
    private static final int PREVIEW_FEET = 75;
    private static final int PREVIEW_LOOK = 25;
    private static final int PREVIEW_SCALE = 30;
    private static final int PREVIEW_REACH = 60;

    private BetaScreens() {
    }

    public static boolean active() {
        return BetaModels.packActive();
    }

    public static void layOut(AbstractContainerScreen<?> screen) {
        boolean beta = active();
        AbstractContainerScreenAccessor labels = (AbstractContainerScreenAccessor) screen;
        if (screen instanceof InventoryScreen inventory) {
            layOutInventory(inventory.getMenu(), beta);
            labels.b173$setTitleLabelX(beta ? 86 : 97);
            labels.b173$setTitleLabelY(beta ? 16 : 6);
        } else if (!beta) {
            return;
        } else if (screen instanceof CraftingScreen) {
            labels.b173$setTitleLabelX(28);
        } else if (screen instanceof FurnaceScreen || screen instanceof DispenserScreen) {
            labels.b173$setTitleLabelX(60);
        }
    }

    private static void layOutInventory(InventoryMenu menu, boolean beta) {
        List<Slot> slots = menu.slots;
        if (slots.size() <= InventoryMenu.SHIELD_SLOT) {
            return;
        }
        ((BetaSlot) slots.get(InventoryMenu.RESULT_SLOT)).b173$moveTo(beta ? 144 : 154, beta ? 36 : 28);
        for (int i = 0; i < InventoryMenu.CRAFT_SLOT_COUNT; i++) {
            ((BetaSlot) slots.get(InventoryMenu.CRAFT_SLOT_START + i)).b173$moveTo(
                    (beta ? 88 : 98) + i % 2 * 18, (beta ? 26 : 18) + i / 2 * 18);
        }
        ((BetaSlot) slots.get(InventoryMenu.SHIELD_SLOT)).b173$setHidden(beta);
    }

    public static void inventoryPreview(GuiGraphicsExtractor graphics, int left, int top, float mouseX, float mouseY,
                                        LivingEntity player) {
        int x = left + PREVIEW_X;
        int y = top + PREVIEW_LOOK;
        float height = player.getBbHeight() / player.getScale();
        float yOffset = (float) (PREVIEW_FEET - PREVIEW_LOOK) / (float) PREVIEW_SCALE - height / 2.0F;
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x - PREVIEW_REACH, y - PREVIEW_REACH,
                x + PREVIEW_REACH, y + PREVIEW_REACH, PREVIEW_SCALE, yOffset, mouseX, mouseY, player);
    }

    public static int flameHeight(ContainerData furnace) {
        int total = furnace.get(1);
        if (total == 0) {
            total = 200;
        }
        return Math.clamp(furnace.get(0) * 12 / total, 0, 12) + 2;
    }

    public static int arrowWidth(ContainerData furnace) {
        int total = furnace.get(3);
        if (total == 0) {
            return 1;
        }
        return Math.clamp(furnace.get(2) * 24 / total, 0, 23) + 1;
    }

    public static boolean isAdvancementAnnouncement(Component message) {
        return message.getContents() instanceof TranslatableContents key
                && key.getKey().startsWith("chat.type.advancement.");
    }

    public static List<Component> tooltip(ItemStack stack) {
        String name = stack.getHoverName().getString().trim();
        return name.isEmpty() ? List.of() : List.of(Component.literal(name));
    }

    public static void durabilityBar(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        double damage = stack.getDamageValue();
        double max = stack.getMaxDamage();
        int width = (int) Math.round(13.0D - damage * 13.0D / max);
        int green = (int) Math.round(255.0D - damage * 255.0D / max);
        int color = (255 - green) << 16 | green << 8;
        int shade = (255 - green) / 4 << 16 | 0x3F00;
        graphics.fill(RenderPipelines.GUI, x + 2, y + 13, x + 2 + 13, y + 13 + 2, 0xFF000000);
        graphics.fill(RenderPipelines.GUI, x + 2, y + 13, x + 2 + 12, y + 13 + 1, 0xFF000000 | shade);
        graphics.fill(RenderPipelines.GUI, x + 2, y + 13, x + 2 + width, y + 13 + 1, 0xFF000000 | color);
    }
}
