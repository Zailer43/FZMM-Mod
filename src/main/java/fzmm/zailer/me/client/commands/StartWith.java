package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.Text;

public class StartWith {
    public static String startWithMsg = "";

    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("startwith")
                .then(ArgumentBuilders.argument("message", TextArgumentType.text()).executes(ctx -> {

                    Text message = ctx.getArgument("message", Text.class);

                    startWithMsg = message.getString();
                    return 1;
                }));
    }
}
