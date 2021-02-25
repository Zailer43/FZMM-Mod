package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import fzmm.zailer.me.client.ReplaceText;
import net.minecraft.util.Formatting;

public class RemplaceTextCommand {
    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("replacetext").executes(
                ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    if (mc.player == null) return 0;

                    String messageString = Formatting.DARK_AQUA + "Lista de texto que se remplaza cuando lo envias:\n";
                    for (String[] text : ReplaceText.texts)
                        messageString +=  Formatting.GREEN + text[0] + Formatting.YELLOW  + "  ->  " + Formatting.GREEN +  text[1] + "\n";

                    LiteralText message = new LiteralText(messageString);
                    mc.inGameHud.addChatMessage(MessageType.SYSTEM, message, mc.player.getUuid());
                    return 1;
                }
        );
    }
}
