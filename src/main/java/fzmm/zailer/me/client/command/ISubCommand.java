package fzmm.zailer.me.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import java.util.List;

public interface ISubCommand {

    String alias();

    String syntax();

    default LiteralCommandNode<FabricClientCommandSource> build(CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> baseCommandBuilder = ClientCommandManager.literal(this.alias())
                .executes(ctx -> sendHelpMessage(getTranslationKey(), this.syntax()));

        LiteralCommandNode<FabricClientCommandSource> baseCommand = getBaseCommand(registryAccess, baseCommandBuilder);
        List<LiteralCommandNode<FabricClientCommandSource>> subCommands = getSubCommands(registryAccess);

        for (var subCommand : subCommands) {
            baseCommand.addChild(subCommand);
        }

        return baseCommand;
    }

    LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess,
                                                                 LiteralArgumentBuilder<FabricClientCommandSource> builder);

    default List<LiteralCommandNode<FabricClientCommandSource>> getSubCommands(CommandBuildContext registryAccess) {
        return List.of();
    }

    default int sendHelpMessage(String infoTranslationKey, String syntax) {
        return FzmmCommand.sendHelpMessage(infoTranslationKey, syntax);
    }

    default String getTranslationKey() {
        return "commands.fzmm." + this.alias() + ".help";
    }
}
