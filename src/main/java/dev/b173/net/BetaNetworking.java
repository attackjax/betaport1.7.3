package dev.b173.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BetaNetworking {
    private BetaNetworking() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(BetaWorldPayload.TYPE, BetaWorldPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            BetaWorldPayload payload = BetaWorldPayload.of(server);
            if (payload != null && ServerPlayNetworking.canSend(handler, BetaWorldPayload.TYPE)) {
                sender.sendPacket(payload);
            }
        });
    }
}
