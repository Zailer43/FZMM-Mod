package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.ItemEnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ItemCommand {

    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        return ArgumentBuilders.literal("item")
                .then(ArgumentBuilders.literal("name")
                        .then(ArgumentBuilders.argument("name", TextArgumentType.text()).executes(ctx -> {

                            Text name = ctx.getArgument("name", Text.class);

                            name = replaceColorCodes(name);
                            renameItem(name);
                            return 1;
                        }))
                ).then(ArgumentBuilders.literal("give")
                        .then(ArgumentBuilders.argument("item", ItemStackArgumentType.itemStack()).executes((ctx) -> {

                            giveItem(ItemStackArgumentType.getItemStackArgument(ctx, "item"));
                            return 1;
                        }))
                ).then(ArgumentBuilders.literal("enchant")
                        .then(ArgumentBuilders.argument("enchantment", ItemEnchantmentArgumentType.itemEnchantment()).executes(ctx -> {

                            Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);

                            addEnchant(enchant, 127);
                            return 1;
                        }).then(ArgumentBuilders.argument("level", IntegerArgumentType.integer(-127, 127)).executes(ctx -> {

                            Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);
                            int level = ctx.getArgument("level", int.class);

                            addEnchant(enchant, level);
                            return 1;
                        })))
                );
    }


    public static Text replaceColorCodes(Text message) {
        String messageString = message.getString();
        messageString = messageString.replaceAll("&&", "§");
        message = new LiteralText(messageString);
        return message;
    }


    public static void renameItem(Text name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ItemStack stack = mc.player.inventory.getMainHandStack();
        stack.setCustomName(name);
        mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
    }

    public static void giveItem(ItemStackArgument item) throws CommandSyntaxException {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ItemStack itemStack = item.createStack(1, false);
        mc.player.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    public static void addEnchant(Enchantment enchant, int level) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ItemStack stack = mc.player.inventory.getMainHandStack();
        stack.addEnchantment(enchant, level);
        mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
    }
}