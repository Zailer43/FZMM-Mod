package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

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
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();

            this.setEnchant(enchant, (short) 1);
            return 1;

        }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();
            int level = ctx.getArgument("level", int.class);

            this.setEnchant(enchant, (short) level);
            return 1;
        }))).build();
    }

    private void setEnchant(Enchantment enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);

        stack.apply(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT, component -> {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
            builder.set(enchant, level);
            return builder.build();
        });

        ItemUtils.give(stack);
    }
}
