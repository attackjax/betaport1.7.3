package dev.b173.client;

import dev.b173.client.block.BetaBlockModelPlugin;
import dev.b173.client.gui.BetaScreens;
import dev.b173.client.visual.BetaAtmosphere;
import dev.b173.client.visual.BetaVisuals;
import dev.b173.net.BetaWorldPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class Beta173Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BetaWorldPayload.TYPE,
                (payload, context) -> BetaClientState.accept(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BetaClientState.clear());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (dev.b173.client.gui.BetaScreens.active()) {
                dev.b173.client.gui.BetaDifficulty.apply(client);
            }
        });
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> BetaClientState.refreshSize(entity));
        BetaTints.register();
        BetaVisuals.register();
        BetaHud.register();
        ModelLoadingPlugin.register(new BetaBlockModelPlugin());
        ClientTickEvents.END_CLIENT_TICK.register(BetaAtmosphere::tick);
        ClientTickEvents.END_CLIENT_TICK.register(dev.b173.client.visual.BetaSmoothLighting::tick);
        dev.b173.client.gui.BetaKeys.register();
        ClientTickEvents.END_CLIENT_TICK.register(dev.b173.client.gui.BetaKeys::tick);
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) ->
                !(BetaScreens.active() && BetaScreens.isAdvancementAnnouncement(message)));
    }
}
