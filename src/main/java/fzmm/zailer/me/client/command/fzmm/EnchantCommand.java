package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class EnchantCommand implements ISubCommand {
    @Override
    public String alias() {
        return "enchant";
    }

    @Override
    public String syntax() {
        return "enchant <enchantment> <level>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("enchantment", ResourceArgument.resource(registryAccess, Registries.ENCHANTMENT)).executes(ctx -> {
            @SuppressWarnings("unchecked")
            Holder.Reference<Enchantment> enchant = ctx.getArgument("enchantment", Holder.Reference.class);

            this.setEnchant(enchant, (short) 1);
            return 1;

        }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Holder.Reference<Enchantment> enchant = ctx.getArgument("enchantment", Holder.Reference.class);
            int level = ctx.getArgument("level", int.class);

            this.setEnchant(enchant, (short) level);
            return 1;
        }))).build();
    }

    private void setEnchant(Holder.Reference<Enchantment> enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        stack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, component -> {
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(component);
            builder.set(enchant, level);
            return builder.toImmutable();
        });

        ItemUtils.give(stack);
    }
}
