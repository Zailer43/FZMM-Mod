package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;

public class Commands implements ClientCommandPlugin {
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {

        LiteralArgumentBuilder<CottonClientCommandSource> fzmm = ArgumentBuilders.literal("fzmm");

        fzmm.then(PingCommand.getArgumentBuilder());
        fzmm.then(CopyCoordsCommand.getArgumentBuilder());
        fzmm.then(RemplaceTextCommand.getArgumentBuilder());
        fzmm.then(ItemCommand.getArgumentBuilder());

        dispatcher.register(fzmm);
    }
}
