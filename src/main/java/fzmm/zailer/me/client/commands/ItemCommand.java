package fzmm.zailer.me.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemCommand {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
        LiteralArgumentBuilder<CottonClientCommandSource> itemCommand = ArgumentBuilders.literal("item");

        itemCommand.then(ArgumentBuilders.literal("name")
                .then(ArgumentBuilders.argument("name", TextArgumentType.text()).executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    name = replaceColorCodes(name);
                    renameItem(name);
                    return 1;
                }))
        );

        itemCommand.then(ArgumentBuilders.literal("give")
                .then(ArgumentBuilders.argument("item", ItemStackArgumentType.itemStack()).executes((ctx) -> {

                    giveItem(ItemStackArgumentType.getItemStackArgument(ctx, "item"), 1);
                    return 1;

                }).then(ArgumentBuilders.argument("amount", IntegerArgumentType.integer(1, 127)).executes((ctx) -> {

                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                    ItemStackArgument item = ItemStackArgumentType.getItemStackArgument(ctx, "item");

                    giveItem(item, amount);
                    return 1;
                })))

        );

        itemCommand.then(ArgumentBuilders.literal("enchant")
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

        itemCommand.then(ArgumentBuilders.literal("nbt")
                .executes(ctx -> {
                    displayNbt();
                    return 1;
                })
        );

        itemCommand.then(ArgumentBuilders.literal("hat")
                .executes(ctx -> {
                    assert MC.player != null;

                    ItemStack stack = MC.player.inventory.getMainHandStack();
                    MC.player.equipStack(EquipmentSlot.HEAD, stack);
                    return 1;
                })
        );

        itemCommand.then(ArgumentBuilders.literal("overstack")
                        .then(ArgumentBuilders.argument("amount", IntegerArgumentType.integer(2, 127)).executes(ctx -> {

                            int amount = ctx.getArgument("amount", int.class);
                            overStack(amount);
                            return 1;

                        }))
        );

        itemCommand.then(ArgumentBuilders.literal("head")
                .then(ArgumentBuilders.argument("skull owner", StringArgumentType.greedyString()).executes(ctx -> {

                    String skullOwner = ctx.getArgument("skull owner", String.class);
                    getHead(skullOwner);
                    return 1;

                }))
        );

        itemCommand.then(ArgumentBuilders.literal("fullcontainer")
                .then(ArgumentBuilders.argument("slots to fill", IntegerArgumentType.integer(1, 27)).executes(ctx -> {

                    fullContainer(ctx.getArgument("slots to fill", int.class), 0);
                    return 1;

                }).then(ArgumentBuilders.argument("first slot", IntegerArgumentType.integer(0, 27)).executes(ctx -> {

                    int slotsToFill = ctx.getArgument("slots to fill", int.class);
                    int firstSlot = ctx.getArgument("first slot", int.class);

                    fullContainer(slotsToFill, firstSlot);
                    return 1;

                })))
        );

        itemCommand.then(ArgumentBuilders.literal("lock")
                .then(ArgumentBuilders.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

                    String key = ctx.getArgument("key", String.class);
                    key = replaceColorCodes(new LiteralText(key)).getString();
                    lockContainer(key);
                    return 1;

                }))
        );

        return itemCommand;
    }

    private static Text replaceColorCodes(Text message) {
        String messageString = message.getString();
        messageString = messageString.replaceAll("&", "§");
        messageString = messageString.replaceAll("§§", "&");
        message = new LiteralText(messageString);
        return message;
    }

    private static void renameItem(Text name) {
        assert MC.player != null;

        ItemStack stack = MC.player.inventory.getMainHandStack();
        stack.setCustomName(name);
        MC.player.equipStack(EquipmentSlot.MAINHAND, stack);
    }

    private static void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        assert MC.player != null;

        ItemStack itemStack = item.createStack(amount, false);
        MC.player.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    private static void addEnchant(Enchantment enchant, int level) {
        assert MC.player != null;

        ItemStack stack = MC.player.inventory.getMainHandStack();
        stack.addEnchantment(enchant, level);
        MC.player.equipStack(EquipmentSlot.MAINHAND, stack);
    }

    private static void displayNbt() {
        assert MC.player != null;
        ItemStack stack = MC.player.inventory.getMainHandStack();

        if (stack.getTag() == null) {
            LiteralText message = new LiteralText(Formatting.RED + "Ese item no tiene NBT");
            MC.inGameHud.addChatMessage(MessageType.SYSTEM, message, MC.player.getUuid());
            return;
        }
        String nbt = stack.getTag().toString().replaceAll("§", "&");

        MutableText message = new LiteralText(stack.toString() + ": " + nbt)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MutableText length = new LiteralText(Formatting.BLUE + "Length: " + Formatting.DARK_AQUA  + nbt.length())
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(nbt.length())))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, message, MC.player.getUuid());

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, length, MC.player.getUuid());
    }

    private static void overStack(int amount) {
        assert MC.player != null;

        ItemStack stack = MC.player.inventory.getMainHandStack();

        stack.setCount(amount);

        MC.player.equipStack(EquipmentSlot.MAINHAND, stack);
    }

    private static void getHead(String skullOwner) {
        assert MC.player != null;

        ItemStack itemStack = new ItemStack(Registry.ITEM.get(new Identifier("player_head")));

        CompoundTag tag = new CompoundTag();

        tag.putString("SkullOwner", skullOwner);

        itemStack.setTag(tag);
        MC.player.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    private static void fullContainer(int slotsToFill, int firstSlots) {
        assert MC.player != null;

        //{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:stone",Count:1b}],id:"minecraft:dispenser"}}

        ItemStack containerItemStack = MC.player.inventory.getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        CompoundTag tag = new CompoundTag();
        CompoundTag blockEntityTag = new CompoundTag();
        ListTag items = new ListTag();

        for (int i = 0; i != slotsToFill; i++) {
            CompoundTag tagItems = new CompoundTag();

            tagItems.putInt("Slot", i + firstSlots);
            tagItems.putString("id", itemStack.getItem().toString());
            tagItems.putInt("Count", itemStack.getCount());
            if (!(itemStack.getTag() == null)) tagItems.put("tag", itemStack.getTag());

            items.addTag(i, tagItems);
        }

        if (!(containerItemStack.getTag() == null)) {
            tag = containerItemStack.getTag();

            if (!(containerItemStack.getTag().getCompound("BlockEntityTag") == null)) tag.getCompound("BlockEntityTag").put("Items", items);
            else {
                blockEntityTag.put("Items", items);
                tag.put("BlockEntityTag", blockEntityTag);
            }

        } else {
            blockEntityTag.put("Items", items);
            blockEntityTag.putString("id", containerItemStack.getItem().toString());
        }

        if (!(containerItemStack.getTag().getCompound("BlockEntityTag") == null)) {
            tag.getCompound("BlockEntityTag").put("Items", items);
            tag.getCompound("BlockEntityTag").putString("id", containerItemStack.getItem().toString());
        } else tag.put("BlockEntityTag", blockEntityTag);

        containerItemStack.setTag(tag);
        MC.player.equipStack(EquipmentSlot.MAINHAND, containerItemStack);
    }

    private static void lockContainer(String key) {
        assert MC.player != null;

        //{BlockEntityTag:{Lock:"abc"}}

        ItemStack containerItemStack = MC.player.inventory.getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        CompoundTag tag = new CompoundTag();
        CompoundTag blockEntityTag = new CompoundTag();

        if (!(containerItemStack.getTag() == null)) {
            tag = containerItemStack.getTag();

            if (!(containerItemStack.getTag().getCompound("BlockEntityTag") == null)) tag.getCompound("BlockEntityTag").putString("Lock", key);
            else {
                blockEntityTag.putString("Lock", key);
                tag.put("BlockEntityTag", blockEntityTag);
            }

        } else {
            blockEntityTag.putString("Lock", key);
            tag.put("BlockEntityTag", blockEntityTag);
        }

        containerItemStack.setTag(tag);
        itemStack.setCustomName(new LiteralText(key));

        MC.player.equipStack(EquipmentSlot.MAINHAND, containerItemStack);
        MC.player.equipStack(EquipmentSlot.OFFHAND, itemStack);
    }
}