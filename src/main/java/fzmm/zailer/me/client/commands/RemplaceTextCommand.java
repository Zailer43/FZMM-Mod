package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fzmm.zailer.me.config.FzmmConfig;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class RemplaceTextCommand {
    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("replacetext").executes(
            ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
                if (mc.player == null) return 0;

                String messageString = Formatting.DARK_AQUA + new LiteralText("commands.fzmm.replaceText.message").getString() + "\n";

                for (FzmmConfig.Pair text : config.replaceTexts.texts)
                    messageString +=  Formatting.GREEN + text.getOriginal() + Formatting.YELLOW  + "  ->  " + Formatting.GREEN +  text.getReplace() + "\n";

                LiteralText message = new LiteralText(messageString);
                mc.inGameHud.addChatMessage(MessageType.SYSTEM, message, mc.player.getUuid());
                return 1;
            }
        );
    }
}
