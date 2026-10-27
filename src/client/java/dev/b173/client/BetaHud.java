package dev.b173.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;

final class BetaHud {
    private static final Identifier ARMOR_FULL = Identifier.withDefaultNamespace("hud/armor_full");
    private static final Identifier ARMOR_HALF = Identifier.withDefaultNamespace("hud/armor_half");
    private static final Identifier ARMOR_EMPTY = Identifier.withDefaultNamespace("hud/armor_empty");
    private static final Identifier AIR = Identifier.withDefaultNamespace("hud/air");
    private static final Identifier AIR_BURSTING = Identifier.withDefaultNamespace("hud/air_bursting");

    private BetaHud() {
    }

    static void register() {
        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, original -> (graphics, delta) -> {
            if (!BetaClientState.onBetaServer()) {
                original.extractRenderState(graphics, delta);
                return;
            }
            graphics.pose().pushMatrix();
            graphics.pose().translate(0.0F, 7.0F);
            original.extractRenderState(graphics, delta);
            graphics.pose().popMatrix();
        });
        betaOnly(VanillaHudElements.ARMOR_BAR, BetaHud::armor);
        betaOnly(VanillaHudElements.AIR_BAR, BetaHud::air);
        betaOnly(VanillaHudElements.FOOD_BAR, null);
        betaOnly(VanillaHudElements.INFO_BAR, null);
        betaOnly(VanillaHudElements.EXPERIENCE_LEVEL, null);
        betaOnly(VanillaHudElements.HELD_ITEM_TOOLTIP, null);
        betaOnly(VanillaHudElements.MOUNT_HEALTH, null);
    }

    private static void betaOnly(Identifier element, HudElement beta) {
        HudElementRegistry.replaceElement(element, original -> (graphics, delta) -> {
            if (!BetaClientState.onBetaServer()) {
                original.extractRenderState(graphics, delta);
            } else if (beta != null) {
                beta.extractRenderState(graphics, delta);
            }
        });
    }

    private static Player survivalPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return null;
        }
        return minecraft.player;
    }

    private static void armor(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Player player = survivalPlayer();
        if (player == null) {
            return;
        }
        int armor = player.getArmorValue();
        if (armor <= 0) {
            return;
        }
        int y = graphics.guiHeight() - 32;
        for (int i = 0; i < 10; i++) {
            int x = graphics.guiWidth() / 2 + 91 - i * 8 - 9;
            Identifier sprite = i * 2 + 1 < armor ? ARMOR_FULL : i * 2 + 1 == armor ? ARMOR_HALF : ARMOR_EMPTY;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
        }
    }

    private static void air(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Player player = survivalPlayer();
        if (player == null || !player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        int air = player.getAirSupply();
        int full = (int) Math.ceil((double) (air - 2) * 10.0D / 300.0D);
        int bursting = (int) Math.ceil((double) air * 10.0D / 300.0D) - full;
        int y = graphics.guiHeight() - 32 - 9;
        for (int i = 0; i < full + bursting; i++) {
            int x = graphics.guiWidth() / 2 - 91 + i * 8;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, i < full ? AIR : AIR_BURSTING, x, y, 9, 9);
        }
    }
}
