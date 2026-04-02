package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class FakeEnchantCommand implements ISubCommand {
    @Override
    public String alias() {
        return "fakeenchant";
    }

    @Override
    public String syntax() {
        return "fakeenchant <enchantment> <level>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommands.argument("enchantment", ResourceArgument.resource(registryAccess, Registries.ENCHANTMENT)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Holder.Reference<Enchantment> enchant = ctx.getArgument("enchantment", Holder.Reference.class);

            this.addFakeEnchant(enchant, 1);
            return 1;
        }).then(ClientCommands.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Holder.Reference<Enchantment> enchant = ctx.getArgument("enchantment", Holder.Reference.class);
            int level = ctx.getArgument("level", int.class);

            this.addFakeEnchant(enchant, level);
            return 1;
        }))).build();
    }

    private void addFakeEnchant(Holder.Reference<Enchantment> enchant, int level) {
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        stack.update(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, null, component -> true);

        stack.update(DataComponents.LORE, ItemLore.EMPTY, component -> {
            List<Component> lines = new ArrayList<>();

            MutableComponent enchantMessage = Enchantment.getFullname(enchant, level).copy();
            enchantMessage = Enchantment.getFullname(enchant, level).copy().setStyle(enchantMessage.getStyle().withItalic(false));
            Style style = enchantMessage.getStyle();

            enchantMessage.getSiblings().forEach(text -> {
                if (!text.getString().isBlank())
                    ((MutableComponent) text).setStyle(style);
            });

            lines.add(enchantMessage);
            lines.addAll(component.lines());

            return new ItemLore(List.copyOf(lines));
        });

        ItemUtils.give(stack);
    }
}
