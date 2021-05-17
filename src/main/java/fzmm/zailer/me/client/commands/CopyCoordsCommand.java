package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class CopyCoordsCommand {
    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("copycoords").executes(source -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return 1;

            long x = Math.round(mc.player.getX());
            long y = Math.round(mc.player.getY());
            long z = Math.round(mc.player.getZ());
            String coords =  x + " " + y + " " + z;

            MutableText message = new LiteralText(Formatting.GREEN + coords)
                    .setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, coords))
                    );

            mc.inGameHud.addChatMessage(MessageType.SYSTEM, message, mc.player.getUuid());
            return 1;
        });
    }
}
