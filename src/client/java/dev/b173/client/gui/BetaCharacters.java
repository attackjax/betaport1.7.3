package dev.b173.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.IntPredicate;

public final class BetaCharacters {
    private static final Identifier FONT_TXT = Identifier.fromNamespaceAndPath("b173", "font.txt");

    private BetaCharacters() {
    }

    public static IntPredicate allowed(Minecraft minecraft) {
        Optional<Resource> resource = minecraft.getResourceManager().getResource(FONT_TXT);
        if (resource.isPresent()) {
            StringBuilder characters = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().open(),
                    StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    if (!line.startsWith("#")) {
                        characters.append(line);
                    }
                }
                String beta = characters.toString();
                return character -> beta.indexOf(character) >= 0;
            } catch (IOException ignored) {
            }
        }
        return StringUtil::isAllowedChatCharacter;
    }
}
