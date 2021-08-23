package fzmm.zailer.me.client;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fzmm.zailer.me.utils.FzmmUtils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class FzmmCommand {

    static final CommandException ERROR_WITHOUT_NBT = new CommandException(new TranslatableText("commands.fzmm.item.withoutNbt"));
    static final CommandException ERROR_CONFIG_NOT_FOUND = new CommandException(new TranslatableText("commands.fzmm.item.lore.fromConfig.notFound"));

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static void registerCommands() {
        LiteralArgumentBuilder<FabricClientCommandSource> fzmmCommand = ClientCommandManager.literal("fzmm");

        fzmmCommand.then(ClientCommandManager.literal("name"))
                .then(ClientCommandManager.argument("name", TextArgumentType.text()).executes(ctx -> {

            Text name = ctx.getArgument("name", Text.class);

            renameItem(name);
            return 1;
        }));

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("message", TextArgumentType.text()).executes(ctx -> {

                            Text message = ctx.getArgument("message", Text.class);

                            addLore(message);
                            return 1;
                        }))
                ).then(ClientCommandManager.literal("addfromconfig")
                        .then(ClientCommandManager.argument("config key", StringArgumentType.word()).executes(ctx -> {

                            addLoreFromConfig(ctx.getArgument("config key", String.class));
                            return 1;
                        }))
                ).then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, 32767)).executes(ctx -> {

                            removeLore(ctx.getArgument("line", int.class));
                            return 1;
                        }))
                )
        );


        fzmmCommand.then(ClientCommandManager.literal("give")
                .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack()).executes((ctx) -> {

                    giveItem(ItemStackArgumentType.getItemStackArgument(ctx, "item"), 1);
                    return 1;

                }).then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 127)).executes((ctx) -> {

                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                    ItemStackArgument item = ItemStackArgumentType.getItemStackArgument(ctx, "item");

                    giveItem(item, amount);
                    return 1;
                })))

        );

        fzmmCommand.then(ClientCommandManager.literal("enchant")
                .then(ClientCommandManager.argument("enchantment", EnchantmentArgumentType.enchantment()).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);

                    addEnchant(enchant, 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);
                    int level = ctx.getArgument("level", int.class);

                    addEnchant(enchant, level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("nbt")
                .executes(ctx -> {
                    displayNbt();
                    return 1;
                })
        );

        fzmmCommand.then(ClientCommandManager.literal("amount")
                .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 64)).executes(ctx -> {

                    int amount = ctx.getArgument("amount", int.class);
                    amount(amount);
                    return 1;

                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("skull")
                .then(ClientCommandManager.argument("skull owner", StringArgumentType.greedyString()).suggests(FzmmUtils.SUGGESTION_PLAYER)
                        .executes(ctx -> {

                            String skullOwner = ctx.getArgument("skull owner", String.class);
                            getHead(skullOwner);
                            return 1;

                        }))
        );

        fzmmCommand.then(ClientCommandManager.literal("fullcontainer")
                .then(ClientCommandManager.argument("slots to fill", IntegerArgumentType.integer(1, 27)).executes(ctx -> {

                    fullContainer(ctx.getArgument("slots to fill", int.class), 0);
                    return 1;

                }).then(ClientCommandManager.argument("first slot", IntegerArgumentType.integer(0, 27)).executes(ctx -> {

                    int slotsToFill = ctx.getArgument("slots to fill", int.class);
                    int firstSlot = ctx.getArgument("first slot", int.class);

                    fullContainer(slotsToFill, firstSlot);
                    return 1;

                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("lock")
                .then(ClientCommandManager.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

                    String key = ctx.getArgument("key", String.class);
                    lockContainer(key);
                    return 1;

                }))
        );

        ClientCommandManager.DISPATCHER.register(
                fzmmCommand
        );
    }

    private static void renameItem(Text name) {
        assert MC.player != null;

        ItemStack stack = MC.player.getInventory().getMainHandStack();
        stack.setCustomName(name);
        FzmmUtils.giveItem(stack);
    }

    private static void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        assert MC.player != null;

        ItemStack itemStack = item.createStack(amount, false);
        FzmmUtils.giveItem(itemStack);
    }

    private static void addEnchant(Enchantment enchant, int level) {
        assert MC.player != null;

        //{Enchantments:[{id:"minecraft:aqua_affinity",lvl:1s}]}

        ItemStack stack = MC.player.getInventory().getMainHandStack();
        NbtCompound tag = stack.getOrCreateNbt();
        NbtList enchantments = new NbtList();

        if (tag.getList("Enchantments", 10) != null) {
            enchantments = tag.getList("Enchantments", 10);
        }
        enchantments.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchant), (short) level));

        tag.put("Enchantments", enchantments);
        stack.setNbt(tag);
        FzmmUtils.giveItem(stack);
    }

    private static void displayNbt() {
        assert MC.player != null;
        ItemStack stack = MC.player.getInventory().getMainHandStack();

        if (stack.getNbt() == null) {
            throw ERROR_WITHOUT_NBT;
        }
        String nbt = stack.getNbt().toString().replaceAll("§", "\u00a7");

        MutableText message = new LiteralText(stack + ": " + nbt)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MutableText length = new LiteralText(Formatting.BLUE + "Length: " + Formatting.DARK_AQUA + nbt.length())
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(nbt.length())))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, message, MC.player.getUuid());

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, length, MC.player.getUuid());
    }

    private static void amount(int amount) {
        assert MC.player != null;

        ItemStack stack = MC.player.getInventory().getMainHandStack();

        stack.setCount(amount);

        FzmmUtils.giveItem(stack);
    }

    private static void getHead(String skullOwner) {
        assert MC.player != null;

        ItemStack itemStack = new ItemStack(Registry.ITEM.get(new Identifier("player_head")));

        NbtCompound tag = new NbtCompound();

        tag.putString("SkullOwner", skullOwner);

        itemStack.setNbt(tag);
        FzmmUtils.giveItem(itemStack);
    }

    private static void fullContainer(int slotsToFill, int firstSlots) {
        assert MC.player != null;

        //{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:stone",Count:1b}],id:"minecraft:dispenser"}}

        ItemStack containerItemStack = MC.player.getInventory().getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList items = fillSlots(new NbtList(), itemStack, slotsToFill, firstSlots);

        blockEntityTag.put("Items", items);
        blockEntityTag.putString("id", containerItemStack.getItem().toString());

        if (!(containerItemStack.getNbt() == null)) {
            tag = containerItemStack.getNbt();

            if (!(containerItemStack.getNbt().getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY) == null)) {
                items = fillSlots(tag.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY).getList("Items", 10), itemStack, slotsToFill, firstSlots);
                blockEntityTag.put("Items", items);
            }
        }

        tag.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTag);
        containerItemStack.setNbt(tag);
        FzmmUtils.giveItem(containerItemStack);
    }

    private static NbtList fillSlots(NbtList nbtList, ItemStack itemStack, int slotsToFill, int firstSlot) {
        for (int i = 0; i != slotsToFill; i++) {
            NbtCompound tagItems = new NbtCompound();

            tagItems.putInt("Slot", i + firstSlot);
            tagItems.putString("id", itemStack.getItem().toString());
            tagItems.putInt("Count", itemStack.getCount());
            if (!(itemStack.getNbt() == null)) tagItems.put("tag", itemStack.getNbt());

            nbtList.add(tagItems);
        }
        return nbtList;
    }

    private static void lockContainer(String key) {
        assert MC.player != null;

        //{BlockEntityTag:{Lock:"abc"}}

        ItemStack containerItemStack = MC.player.getInventory().getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        if (!(containerItemStack.getNbt() == null)) {
            tag = containerItemStack.getNbt();

            if (!(containerItemStack.getNbt().getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY) == null))
                tag.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY).putString("Lock", key);
            else {
                blockEntityTag.putString("Lock", key);
                tag.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTag);
            }

        } else {
            blockEntityTag.putString("Lock", key);
            tag.put(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTag);
        }

        containerItemStack.setNbt(tag);
        itemStack.setCustomName(new LiteralText(key));

        FzmmUtils.giveItem(containerItemStack);
        MC.player.equipStack(EquipmentSlot.OFFHAND, itemStack);
    }

    private static void addLore(Text message) {
        assert MC.player != null;

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

        ItemStack itemStack = MC.player.getMainHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(message)));

        display.put(ItemStack.LORE_KEY, lore);

        if (!(itemStack.getNbt() == null)) {
            tag = itemStack.getNbt();

            if (!(itemStack.getNbt().getCompound(ItemStack.DISPLAY_KEY) == null)) {
                lore = tag.getCompound(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, 8);
                lore.add(NbtString.of(Text.Serializer.toJson(message)));
                display.put(ItemStack.LORE_KEY, lore);
                display.putString(ItemStack.NAME_KEY, tag.getCompound(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));
            }
        }

        tag.put(ItemStack.DISPLAY_KEY, display);
        itemStack.setNbt(tag);
        FzmmUtils.giveItem(itemStack);
    }

    private static void addLoreFromConfig(String configName) {
        assert MC.player != null;

        //	[
        //		"&9-------",
        //		"&a[TEST]",
        //		"&9-------"
        //	]

        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(MC.runDirectory.toPath().toString(), "config", "fzmm", "lores.json"));
            Map<?, ?> map = gson.fromJson(reader, Map.class);
            boolean configFound = false;
            ArrayList<String> loreArrayString = new ArrayList<>();
            ArrayList<NbtString> loreArray = new ArrayList<>();
            ItemStack itemStack = MC.player.getMainHandStack();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey().equals(configName)) {
                    loreArrayString = (ArrayList<String>) entry.getValue();
                    configFound = true;
                }
            }

            reader.close();

            for (String lore : loreArrayString) {
                loreArray.add(NbtString.of(lore));
            }

            if (!configFound) {
                throw ERROR_CONFIG_NOT_FOUND;
            }

            itemStack.setNbt(FzmmUtils.addLores(itemStack, loreArray));

            FzmmUtils.giveItem(itemStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeLore(int lineToRemove) {
        assert MC.player != null;

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

        ItemStack itemStack = MC.player.getMainHandStack();

        if (!(itemStack.getNbt() == null)) {
            NbtCompound tag = itemStack.getNbt();

            if (!(itemStack.getNbt().getCompound(ItemStack.DISPLAY_KEY) == null)) {
                NbtCompound display = new NbtCompound();
                NbtList lore;

                lore = tag.getCompound(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, 8);
                lore.remove(lineToRemove);
                display.put(ItemStack.LORE_KEY, lore);
                display.putString(ItemStack.NAME_KEY, tag.getCompound(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY));

                tag.put(ItemStack.DISPLAY_KEY, display);
                itemStack.setNbt(tag);
                FzmmUtils.giveItem(itemStack);
            }
        }

    }
}