package fzmm.zailer.me.client.command;

import com.mojang.brigadier.CommandDispatcher;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.fzmm.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class FzmmCommand {

    private static final String BASE_COMMAND_ALIAS = "fzmm";
    private static final String BASE_COMMAND = "/" + BASE_COMMAND_ALIAS;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        var commandBuilder = ClientCommandManager.literal(BASE_COMMAND_ALIAS);

        List<ISubCommand> subCommands = List.of(
                new AmountCommand(), //replace with item editor
                new EnchantCommand(), //replace with item editor
                new EquipCommand(), //replace with something more intuitive and fast to use
                new FakeEnchantCommand(), //replace with item editor ?
                new GiveCommand(),
                new LockCommand(), //replace with item editor
                new LoreCommand(), //replace with item editor
                new NameCommand(), //replace with item editor
                new NbtCommand(), //replace with nbt viewer / item editor
                new OldGiveCommand(),
                new RefillContainer(), //replace with item editor
                new SkullCommand() //replace with item editor
        );

        commandBuilder.executes(ctx -> {
            String subcommands = String.join("/", subCommands.stream().map(ISubCommand::alias).toList());
            return sendHelpMessage("commands.fzmm.help", BASE_COMMAND + " " + subcommands);
        });

        var command = commandBuilder.build();

        for (var subCommand : subCommands) {
            command.addChild(subCommand.build(registryAccess));
        }

        dispatcher.getRoot().addChild(command);
    }

    public static int sendHelpMessage(String infoTranslationKey, String syntax) {
        Text infoTranslation = Text.translatable(infoTranslationKey)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text syntaxText = Text.literal(BASE_COMMAND + " " + syntax)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text translation = Text.translatable("commands.fzmm.help.format", infoTranslation, syntaxText)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(translation);
        return 1;
    }
}