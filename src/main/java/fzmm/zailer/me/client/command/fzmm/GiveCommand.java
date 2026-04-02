package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.client.command.argument_type.StackArgumentType;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemInput;

public class GiveCommand implements ISubCommand {
    @Override
    public String alias() {
        return "give";
    }

    @Override
    public String syntax() {
        return "give <item> <amount>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommands.argument("item", StackArgumentType.item(registryAccess)).executes((ctx) -> {

            this.giveItem(StackArgumentType.getItem(ctx, "item"), 1);
            return 1;
        }).then(ClientCommands.argument("amount", IntegerArgumentType.integer(1, 99)).executes((ctx) -> {

            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            ItemInput item = StackArgumentType.getItem(ctx, "item");

            this.giveItem(item, amount);
            return 1;
        }))).build();
    }

    private void giveItem(ItemInput item, int amount) throws CommandSyntaxException {
        ItemUtils.give(ItemUtils.process(item.createItemStack(amount)));
    }
}
