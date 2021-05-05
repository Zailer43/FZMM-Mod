package fzmm.zailer.me.utils;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FzmmUtils {

    public static final SuggestionProvider<CottonClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {

        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.world != null;

        List<AbstractClientPlayerEntity> players = mc.world.getPlayers();

        for (AbstractClientPlayerEntity player: players) {
            builder.suggest(player.getName().getString());
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    public static Text replaceColorCodes(Text message) {
        String messageString = message.toString();
        if (messageString.contains("&")) {
            messageString = messageString.replaceAll("&", "§");
            messageString = messageString.replaceAll("§§", "&");
            message = new LiteralText(messageString);
        }
        return message;
    }


}
