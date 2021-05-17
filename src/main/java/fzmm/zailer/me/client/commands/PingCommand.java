package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.MessageType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.UUID;

public class PingCommand {

    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("ping").executes(
                ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null) return 0;

                    String username = mc.player.getName().getString();

                    displayPing(getPing(username), username);
                    return 1;
                }
        ).then(ArgumentBuilders.argument("username", StringArgumentType.greedyString()).suggests(FzmmUtils.SUGGESTION_PLAYER).executes(ctx -> {

            String username = ctx.getArgument("username", String.class);

            displayPing(getPing(username), username);

            return 1;
        }));
    }
    

    public static int getPing(String username) {

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();

        if (networkHandler == null) return -1;
        if (mc.player == null) return -2;

        UUID playerID = Objects.requireNonNull(networkHandler.getPlayerListEntry(username)).getProfile().getId();
        int ping;
        ping = Objects.requireNonNull(networkHandler.getPlayerListEntry(playerID)).getLatency();
        return ping;
    }

    public static void displayPing(int ping, String username) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        String pingMessage;

        if (ping <= 150) pingMessage = Formatting.GREEN + "";
        else if (ping <= 300) pingMessage = Formatting.DARK_GREEN + "";
        else if (ping <= 600) pingMessage = Formatting.YELLOW + "";
        else if (ping <= 1000) pingMessage = Formatting.RED + "";
        else pingMessage = Formatting.DARK_RED + "";
        pingMessage += ping + " ms";

        MutableText message = new TranslatableText("commands.fzmm.ping.message", username, pingMessage)
            .setStyle(Style.EMPTY.withColor(Formatting.BLUE));
        mc.inGameHud.addChatMessage(MessageType.SYSTEM, message,mc.player.getUuid());
    }
}