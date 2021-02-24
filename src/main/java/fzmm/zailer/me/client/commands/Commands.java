package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;

public class Commands implements ClientCommandPlugin {
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {

        dispatcher.register(ArgumentBuilders
                .literal("fzmm")
                .then(NbtCommand.getArgumentBuilder())
                .then(PingCommand.getArgumentBuilder())
                .then(HatCommand.getArgumentBuilder())
        );
    }
}
