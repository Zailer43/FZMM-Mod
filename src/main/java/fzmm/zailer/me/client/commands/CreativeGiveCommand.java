package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.UUID;


public class CreativeGiveCommand {


    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("ping").executes(
                source -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    getPing(mc.player.getName().toString());
                    return 1;
                }
        ).then(ArgumentBuilders.argument("name", StringArgumentType.word()).executes(ctx -> {
            handleIncomingName(StringArgumentType.getString(ctx, "name"));

            return 1;
        }));
    }

    private static void handleIncomingName(String name) throws CommandSyntaxException {
        if (name.contains(" ")) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "Please only supply 1 argument.")).create();
        }

        if (name.length() < 2) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "The provided username was too short.")).create();
        }

        if (name.length() > 16) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "The provided username was too long.")).create();
        }

        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

        if (networkHandler == null) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "An illegal exception occurred. (NetworkHandler null)")).create();
        }

        PlayerListEntry entry = networkHandler.getPlayerListEntry(MinecraftClient.getInstance().getSession().getProfile().getId());

        if (entry == null) {
            throw new SimpleCommandExceptionType(new LiteralText(Formatting.RED + "An illegal exception occurred. (PlayerList null)")).create();
        }

        getPing(name);
    }

    private static void getPing(String username) {

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();

        if (networkHandler == null) return;
        if (mc.player == null) return;

        UUID playerID = networkHandler.getPlayerListEntry(username).getProfile().getId();
        int ping = 0;
        ping = networkHandler.getPlayerListEntry(playerID).getLatency();

        LiteralText message = new LiteralText(Formatting.YELLOW + "El ping de " + username + " es " + Formatting.GREEN + ping);
        mc.inGameHud.addChatMessage(MessageType.SYSTEM, message,mc.player.getUuid());
    }
}