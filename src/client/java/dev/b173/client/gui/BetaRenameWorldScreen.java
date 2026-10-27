package dev.b173.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BetaRenameWorldScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("Betaport");
    private final Screen parent;
    private final String levelId;
    private final String oldName;
    private BetaTextField name;
    private Button renameButton;

    public BetaRenameWorldScreen(Screen parent, String levelId, String oldName) {
        super(Component.translatable("b173.selectWorld.renameTitle"));
        this.parent = parent;
        this.levelId = levelId;
        this.oldName = oldName;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        this.renameButton = addRenderableWidget(Button.builder(Component.translatable("b173.selectWorld.renameButton"),
                b -> rename()).bounds(left, this.height / 4 + 96 + 12, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("b173.gui.cancel"), b -> onClose())
                .bounds(left, this.height / 4 + 120 + 12, 200, 20).build());
        String before = this.name == null ? this.oldName : this.name.getValue();
        this.name = addRenderableWidget(new BetaTextField(this.font, left, 60, 200, 20, 32,
                BetaCharacters.allowed(this.minecraft), Component.translatable("b173.selectWorld.enterName")));
        this.name.setValue(before);
        this.name.setResponder(text -> this.renameButton.active = !text.trim().isEmpty());
        this.renameButton.active = !before.trim().isEmpty();
        setInitialFocus(this.name);
    }

    @Override
    public void tick() {
        super.tick();
        this.name.tick();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 257 || event.key() == 335) {
            if (this.renameButton.active) {
                rename();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    private void rename() {
        try (LevelStorageSource.LevelStorageAccess access = this.minecraft.getLevelSource().validateAndCreateAccess(this.levelId)) {
            access.renameLevel(this.name.getValue().trim());
        } catch (Exception e) {
            LOGGER.warn("b173: could not rename world {}", this.levelId, e);
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("b173.selectWorld.enterName"), this.width / 2 - 100, 47, 0xFFA0A0A0);
    }
}
