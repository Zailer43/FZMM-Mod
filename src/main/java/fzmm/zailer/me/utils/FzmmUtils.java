package fzmm.zailer.me.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class FzmmUtils {
    public static void UsernameArgumentType(String name) throws CommandSyntaxException {
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
    }
}
