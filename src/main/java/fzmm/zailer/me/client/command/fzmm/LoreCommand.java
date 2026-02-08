package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

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
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.build();
    }

    @Override
    public List<LiteralCommandNode<FabricClientCommandSource>> getSubCommands(CommandBuildContext registryAccess) {
        List<LiteralCommandNode<FabricClientCommandSource>> result = new ArrayList<>();

        result.add(ClientCommandManager.literal("add")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", " lore add <message>"))
                .then(ClientCommandManager.argument("message", ComponentArgument.textComponent(registryAccess)).executes(ctx -> {

                    Component message = ctx.getArgument("message", Component.class);

                    DisplayBuilder.addLoreToHandItem(message.copy());
                    return 1;
                })).build()
        );

        result.add(ClientCommandManager.literal("remove")
                .executes(ctx -> {

                    this.removeLore();
                    return 1;
                }).then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, ItemLore.MAX_LINES - 1)).executes(ctx -> {

                    this.removeLore(ctx.getArgument("line", int.class));
                    return 1;
                })).build()
        );

        return result;
    }

    private void removeLore() {
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        ItemLore loreComponent = stack.getComponents().get(DataComponents.LORE);
        if (loreComponent != null) {
            removeLore(loreComponent.lines().size() - 1);
        }
    }

    private void removeLore(int lineToRemove) {
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        if (!stack.getComponents().has(DataComponents.LORE)) {
            return;
        }

        stack.update(DataComponents.LORE, ItemLore.EMPTY, component -> {
            List<Component> lines = new ArrayList<>(component.lines());

            if (lines.size() < lineToRemove || lines.isEmpty()) {
                return component;
            }

            lines.remove(lineToRemove);

            return new ItemLore(List.copyOf(lines));
        });

        ItemUtils.give(stack);
    }
}
