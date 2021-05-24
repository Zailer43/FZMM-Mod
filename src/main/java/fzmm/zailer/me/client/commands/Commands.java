package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;

public class Commands implements ClientCommandPlugin {
	//TODO: Cambiar todos los client side commands por una GUI

    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {

        LiteralArgumentBuilder<CottonClientCommandSource> fzmm = ArgumentBuilders.literal("fzmm");

        fzmm.then(PingCommand.getArgumentBuilder());
        fzmm.then(CopyCoordsCommand.getArgumentBuilder());
        fzmm.then(RemplaceTextCommand.getArgumentBuilder());
        fzmm.then(ItemCommand.getArgumentBuilder());
        fzmm.then(CompassCommand.getArgumentBuilder());
        fzmm.then(StartWith.getArgumentBuilder());

        dispatcher.register(fzmm);
    }
}
