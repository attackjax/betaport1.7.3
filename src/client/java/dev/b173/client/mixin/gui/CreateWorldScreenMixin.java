package dev.b173.client.mixin.gui;

import dev.b173.client.gui.BetaCharacters;
import dev.b173.client.gui.BetaTextField;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FileUtil;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Unique
    private static final char[] B173_ILLEGAL = {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>',
            '|', '"', ':'};
    @Unique
    private static final int B173_LABEL = 0xFFA0A0A0;

    @Shadow
    @Final
    private WorldCreationUiState uiState;

    @Unique
    private BetaTextField b173$name;
    @Unique
    private BetaTextField b173$seed;
    @Unique
    private Button b173$create;

    @Shadow
    public abstract void popScreen();

    private CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void b173$betaScreen(CallbackInfo ci) {
        ci.cancel();
        b173$betaSettings();
        var allowed = BetaCharacters.allowed(this.minecraft);
        int left = this.width / 2 - 100;
        this.b173$create = addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"),
                button -> b173$createWorld()).bounds(left, this.height / 4 + 96 + 12, 200, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> popScreen())
                .bounds(left, this.height / 4 + 120 + 12, 200, 20).build());
        this.b173$name = addRenderableWidget(new BetaTextField(this.font, left, 60, 200, 20, 32, allowed,
                Component.translatable("selectWorld.enterName")));
        this.b173$name.setValue(this.uiState.getName());
        this.b173$name.setResponder(name -> {
            this.uiState.setName(name);
            b173$betaFolder();
            this.b173$create.active = !name.isEmpty();
        });
        this.b173$seed = addRenderableWidget(new BetaTextField(this.font, left, 116, 200, 20, 0, allowed,
                Component.translatable("selectWorld.enterSeed")));
        this.b173$seed.setValue(this.uiState.getSeed());
        b173$betaFolder();
        this.b173$create.active = !this.b173$name.getValue().isEmpty();
    }

    @Unique
    private void b173$betaSettings() {
        this.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
        this.uiState.setDifficulty(dev.b173.client.gui.BetaDifficulty.get());
        this.uiState.setAllowCommands(false);
        this.uiState.setBonusChest(false);
        this.uiState.setGenerateStructures(true);
        for (WorldCreationUiState.WorldTypeEntry entry : this.uiState.getNormalPresetList()) {
            if (entry.preset() != null && entry.preset().is(WorldPresets.NORMAL)) {
                if (!entry.equals(this.uiState.getWorldType())) {
                    this.uiState.setWorldType(entry);
                }
                break;
            }
        }
    }

    @Unique
    private void b173$betaFolder() {
        WorldCreationUiStateAccessor state = (WorldCreationUiStateAccessor) this.uiState;
        String folder = this.uiState.getName().trim();
        for (char illegal : B173_ILLEGAL) {
            folder = folder.replace(illegal, '_');
        }
        if (folder.isEmpty()) {
            folder = "World";
        }
        if (!FileUtil.isPathPartPortable(folder)) {
            return;
        }
        Path saves = state.b173$savesFolder();
        try {
            while (Files.exists(saves.resolve(folder))) {
                folder = folder + "-";
            }
        } catch (RuntimeException e) {
            return;
        }
        state.b173$setTargetFolder(folder);
    }

    @Unique
    private void b173$createWorld() {
        if (!this.b173$create.active) {
            return;
        }
        long seed = new Random().nextLong();
        String text = this.b173$seed.getValue();
        if (!text.isEmpty()) {
            try {
                long number = Long.parseLong(text);
                if (number != 0L) {
                    seed = number;
                }
            } catch (NumberFormatException e) {
                seed = text.hashCode();
            }
        }
        this.uiState.setName(this.b173$name.getValue());
        b173$betaFolder();
        this.uiState.setSeed(Long.toString(seed));
        b173$betaSettings();
        ((CreateWorldScreenInvoker) this).b173$create();
    }

    @Inject(method = "repositionElements", at = @At("HEAD"), cancellable = true)
    private void b173$relayout(CallbackInfo ci) {
        ci.cancel();
        rebuildWidgets();
    }

    @Inject(method = "setInitialFocus", at = @At("HEAD"), cancellable = true)
    private void b173$focusName(CallbackInfo ci) {
        ci.cancel();
        if (this.b173$name != null) {
            setInitialFocus(this.b173$name);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void b173$keys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() == GLFW.GLFW_KEY_TAB) {
            if (this.b173$name.isFocused()) {
                setFocused(this.b173$seed);
            } else if (this.b173$seed.isFocused()) {
                setFocused(this.b173$name);
            }
            cir.setReturnValue(true);
        } else if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            b173$createWorld();
            cir.setReturnValue(true);
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(getFocused() != null && getFocused().keyPressed(event));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        BetaTextField under = this.b173$name.isMouseOver(event.x(), event.y()) ? this.b173$name
                : this.b173$seed.isMouseOver(event.x(), event.y()) ? this.b173$seed : null;
        setFocused(under);
        return super.mouseClicked(event, doubleClick) || under != null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.b173$name != null) {
            this.b173$name.tick();
            this.b173$seed.tick();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void b173$draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                           CallbackInfo ci) {
        ci.cancel();
        int left = this.width / 2 - 100;
        graphics.centeredText(this.font, I18n.get("selectWorld.create"), this.width / 2, this.height / 4 - 60 + 20,
                0xFFFFFFFF);
        graphics.text(this.font, I18n.get("selectWorld.enterName"), left, 47, B173_LABEL);
        graphics.text(this.font, I18n.get("b173.selectWorld.resultFolder") + " " + this.uiState.getTargetFolder(), left,
                85, B173_LABEL);
        graphics.text(this.font, I18n.get("selectWorld.enterSeed"), left, 104, B173_LABEL);
        graphics.text(this.font, I18n.get("selectWorld.seedInfo"), left, 140, B173_LABEL);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "extractMenuBackground", at = @At("HEAD"), cancellable = true)
    private void b173$background(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ci.cancel();
        super.extractMenuBackground(graphics);
    }
}
