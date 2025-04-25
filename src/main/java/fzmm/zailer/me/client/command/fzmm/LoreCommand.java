package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class LoreCommand implements ISubCommand {
    @Override
    public String alias() {
        return "lore";
    }

    @Override
    public String syntax() {
        return "lore add/remove";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.build();
    }

    @Override
    public List<LiteralCommandNode<FabricClientCommandSource>> getSubCommands(CommandRegistryAccess registryAccess) {
        List<LiteralCommandNode<FabricClientCommandSource>> result = new ArrayList<>();

        result.add(ClientCommandManager.literal("add")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", " lore add <message>"))
                .then(ClientCommandManager.argument("message", TextArgumentType.text(registryAccess)).executes(ctx -> {

                    Text message = ctx.getArgument("message", Text.class);

                    DisplayBuilder.addLoreToHandItem(message.copy());
                    return 1;
                })).build()
        );

        result.add(ClientCommandManager.literal("remove")
                .executes(ctx -> {

                    this.removeLore();
                    return 1;
                }).then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, LoreComponent.MAX_LORES - 1)).executes(ctx -> {

                    this.removeLore(ctx.getArgument("line", int.class));
                    return 1;
                })).build()
        );

        return result;
    }

    private void removeLore() {
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);

        LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            removeLore(loreComponent.lines().size() - 1);
        }
    }

    private void removeLore(int lineToRemove) {
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);

        if (!stack.getComponents().contains(DataComponentTypes.LORE)) {
            return;
        }

        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
            List<Text> lines = new ArrayList<>(component.lines());

            if (lines.size() < lineToRemove || lines.isEmpty()) {
                return component;
            }

            lines.remove(lineToRemove);

            return new LoreComponent(List.copyOf(lines));
        });

        ItemUtils.give(stack);
    }
}
