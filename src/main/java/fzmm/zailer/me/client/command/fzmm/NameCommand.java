package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;

public class NameCommand implements ISubCommand {
    @Override
    public String alias() {
        return "name";
    }

    @Override
    public String syntax() {
        return "name <item name>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommands.argument("name", ComponentArgument.textComponent(registryAccess))
                .executes(ctx -> {

                    Component name = ctx.getArgument("name", Component.class);

                    DisplayBuilder.renameHandItem(name.copy());
                    return 1;
                })).build();
    }
}
