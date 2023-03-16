package fzmm.zailer.me.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import fzmm.zailer.me.utils.skin.GetSkinFromMineskin;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.Optional;

public class FzmmCommand {

    private static final CommandException ERROR_WITHOUT_NBT = new CommandException(Text.translatable("commands.fzmm.item.withoutNbt"));
    private static final String BASE_COMMAND_ALIAS = "fzmm";
    private static final String BASE_COMMAND = "/" + BASE_COMMAND_ALIAS;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> fzmmCommand = ClientCommandManager.literal(BASE_COMMAND_ALIAS);

        fzmmCommand.then(ClientCommandManager.literal("name")
                .executes(ctx -> sendHelpMessage("commands.fzmm.name.help", BASE_COMMAND + " name <item name>"))
                .then(ClientCommandManager.argument("name", TextArgumentType.text()).executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    DisplayBuilder.renameHandItem(name);
                    return 1;
                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.help", BASE_COMMAND + " lore add/remove"))
                .then(ClientCommandManager.literal("add")
                        .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", BASE_COMMAND + " lore add <message>"))
                        .then(ClientCommandManager.argument("id", TextArgumentType.text()).executes(ctx -> {

                            Text message = ctx.getArgument("id", Text.class);

                            DisplayBuilder.addLoreToHandItem(message);
                            return 1;
                        }))
                ).then(ClientCommandManager.literal("remove")
                        .executes(
                                ctx -> {

                                    removeLore();
                                    return 1;
                                }
                        ).then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, 32767)).executes(ctx -> {

                            removeLore(ctx.getArgument("line", int.class));
                            return 1;
                        }))
                )
        );


        fzmmCommand.then(ClientCommandManager.literal("give")
                .executes(ctx -> sendHelpMessage("commands.fzmm.give.help", BASE_COMMAND + " give <item> <amount>"))
                .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes((ctx) -> {

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
                .executes(ctx -> sendHelpMessage("commands.fzmm.enchant.help", BASE_COMMAND + " enchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
                    @SuppressWarnings("unchecked")
                    Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();

                    addEnchant(enchant, (short) 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();
                    int level = ctx.getArgument("level", int.class);

                    addEnchant(enchant, (short) level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("fakeenchant")
                .executes(ctx -> sendHelpMessage("commands.fzmm.fakeenchant.help", BASE_COMMAND + " fakeenchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();

                    addFakeEnchant(enchant, 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();
                    int level = ctx.getArgument("level", int.class);

                    addFakeEnchant(enchant, level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("nbt")
                .executes(ctx -> {
                    showNbt();
                    return 1;
                })
        );

        fzmmCommand.then(ClientCommandManager.literal("amount")
                .executes(ctx -> sendHelpMessage("commands.fzmm.amount.help", BASE_COMMAND + " amount <value>"))
                .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 64)).executes(ctx -> {

                    int amount = ctx.getArgument("value", int.class);
                    amount(amount);
                    return 1;

                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("skull")
                .executes(ctx -> sendHelpMessage("commands.fzmm.skull.help", BASE_COMMAND + " skull <skull owner> cache/mineskin/mojang"))
                .then(ClientCommandManager.argument("skull owner", StringArgumentType.word()).suggests(FzmmUtils.SUGGESTION_PLAYER)
                        .executes(ctx -> {

                            String skullOwner = ctx.getArgument("skull owner", String.class);
                            getHead(new GetSkinFromCache(new GetSkinFromMojang()), skullOwner);
                            return 1;

                        }).then(ClientCommandManager.literal("cache")
                            .executes(ctx -> {

                                String skullOwner = ctx.getArgument("skull owner", String.class);
                                getHead(new GetSkinFromCache(), skullOwner);

                                return 1;
                            })).then(ClientCommandManager.literal("mineskin")
                            .executes(ctx -> {

                                String skullOwner = ctx.getArgument("skull owner", String.class);
                                getHead(new GetSkinFromMineskin().setCacheSkin(skullOwner), skullOwner);

                                return 1;
                            })).then(ClientCommandManager.literal("mojang")
                            .executes(ctx -> {

                                String skullOwner = ctx.getArgument("skull owner", String.class);
                                getHead(new GetSkinFromMojang(), skullOwner);

                                return 1;
                            }))
                )
        );

        fzmmCommand.then(ClientCommandManager.literal("fullcontainer")
                .executes(ctx -> sendHelpMessage("commands.fzmm.fullcontainer.help", BASE_COMMAND + " fullcontainer <slots to fill> <first slot>"))
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
                .executes(ctx -> sendHelpMessage("commands.fzmm.lock.help", BASE_COMMAND + " lock <key>"))
                .then(ClientCommandManager.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

                    String key = ctx.getArgument("key", String.class);
                    lockContainer(key);
                    return 1;

                }))
        );

        fzmmCommand.executes(ctx -> {
            String subcommands = String.join("/", fzmmCommand.getArguments().stream().map(CommandNode::getName).toList());
            return sendHelpMessage("commands.fzmm.help", BASE_COMMAND + " " + subcommands);
        });

        dispatcher.register(fzmmCommand);
    }

    private static int sendHelpMessage(String infoTranslationKey, String syntax) {
        Text infoTranslation = Text.translatable(infoTranslationKey)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text syntaxText = Text.literal(syntax)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text translation = Text.translatable("commands.fzmm.help.format", infoTranslation, syntaxText)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        player.sendMessage(translation);
        return 1;
    }

    private static void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        ItemStack itemStack = item.createStack(amount, false);
        FzmmUtils.giveItem(itemStack);
    }

    private static void addEnchant(Enchantment enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}

        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();
        NbtCompound tag = stack.getOrCreateNbt();
        NbtList enchantments = new NbtList();

        if (tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            enchantments = tag.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        enchantments.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchant), level));

        tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        stack.setNbt(tag);
        FzmmUtils.giveItem(stack);
    }

    private static void addFakeEnchant(Enchantment enchant, int level) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();
        MutableText enchantMessage = (MutableText) enchant.getName(level);

        Style style = enchantMessage.getStyle().withItalic(false);
        enchantMessage.getSiblings().forEach(text -> {
            if (!text.getString().isBlank())
                ((MutableText) text).setStyle(style);
        });

        stack = DisplayBuilder.of(stack).addLore(enchantMessage).get();

        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList enchantments = new NbtList();
            enchantments.add(new NbtCompound());
            tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        }

        FzmmUtils.giveItem(stack);
    }

    private static void showNbt() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getInventory().getMainHandStack();

        if (!stack.hasNbt()) {
            throw ERROR_WITHOUT_NBT;
        }
        assert stack.getNbt() != null;
        String nbt = stack.getNbt().toString();//.replaceAll("§", "\\\\u00a7");

        MutableText message = Text.literal(stack + ": " + nbt)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to copy")))
                );

        MutableText length = Text.literal(Formatting.BLUE + "Length: " + Formatting.DARK_AQUA + nbt.length())
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(nbt.length())))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to copy")))
                );

        ChatHud chatHud = client.inGameHud.getChatHud();
        chatHud.addMessage(message);
        chatHud.addMessage(length);
    }

    private static void amount(int amount) {
        assert MinecraftClient.getInstance().player != null;

        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();

        stack.setCount(amount);

        FzmmUtils.updateHand(stack);
    }

    private static void getHead(GetSkinDecorator skinDecorator, String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        Optional<ItemStack> optionalStack = skinDecorator.getHead(playerName);
        FzmmUtils.giveItem(optionalStack.orElseGet(() -> {
            FzmmClient.LOGGER.warn("[FzmmCommand] Could not get head for {}", playerName);
            return Items.PLAYER_HEAD.getDefaultStack();
        }));
    }

    private static void fullContainer(int slotsToFill, int firstSlots) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        //{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:stone",Count:1b}]}}

        ItemStack containerItemStack = client.player.getInventory().getMainHandStack();
        ItemStack itemStack = client.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList items = fillSlots(new NbtList(), itemStack, slotsToFill, firstSlots);

        blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
        blockEntityTag.putString("id", containerItemStack.getItem().toString());

        if (!(containerItemStack.getNbt() == null)) {
            tag = containerItemStack.getNbt();

            if (!(containerItemStack.getNbt().getCompound(TagsConstant.BLOCK_ENTITY) == null)) {
                items = fillSlots(tag.getCompound(TagsConstant.BLOCK_ENTITY).getList(ShulkerBoxBlockEntity.ITEMS_KEY, 10), itemStack, slotsToFill, firstSlots);
                blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
            }
        }

        tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        containerItemStack.setNbt(tag);
        FzmmUtils.giveItem(containerItemStack);
    }

    private static NbtList fillSlots(NbtList slotsList, ItemStack stack, int slotsToFill, int firstSlot) {
        for (int i = 0; i != slotsToFill; i++) {
            InventoryUtils.addSlot(slotsList, stack, i + firstSlot);
        }
        return slotsList;
    }

    private static void lockContainer(String key) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        //{BlockEntityTag:{Lock:"abc"}}

        ItemStack containerItemStack = client.player.getInventory().getMainHandStack();
        ItemStack itemStack = client.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        if (containerItemStack.hasNbt() || tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
            tag = containerItemStack.getNbt();
            assert tag != null;

            if (tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
                tag.getCompound(TagsConstant.BLOCK_ENTITY).putString("Lock", key);
            }

        } else {
            blockEntityTag.putString("Lock", key);
            tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        }

        containerItemStack.setNbt(tag);
        itemStack.setCustomName(Text.literal(key));

        FzmmUtils.giveItem(containerItemStack);
        assert client.interactionManager != null;
        client.interactionManager.clickCreativeStack(itemStack, PlayerInventory.OFF_HAND_SLOT + PlayerInventory.getHotbarSize());
    }

    private static void removeLore() {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        NbtCompound display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);
        if (display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE)) {
            removeLore(display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE).size() - 1);
        }
    }

    private static void removeLore(int lineToRemove) {
        assert MinecraftClient.getInstance().player != null;

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

        ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack();

        NbtCompound display = itemStack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        if (!display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE))
            return;

        NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        if (lore.size() < lineToRemove)
            return;

        lore.remove(lineToRemove);
        display.put(ItemStack.LORE_KEY, lore);

        itemStack.setSubNbt(ItemStack.DISPLAY_KEY, display);
        FzmmUtils.giveItem(itemStack);
    }
}