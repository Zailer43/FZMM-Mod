package fzmm.zailer.me.client.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fzmm.zailer.me.utils.FzmmUtils;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
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

public class ItemCommand {

	static final CommandException ERROR_WITHOUT_NBT = new CommandException(new TranslatableText("commands.fzmm.item.withoutNbt"));
	static final CommandException ERROR_CONFIG_NOT_FOUND = new CommandException(new TranslatableText("commands.fzmm.item.lore.fromConfig.notFound"));

	private static final MinecraftClient MC = MinecraftClient.getInstance();

	public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
		LiteralArgumentBuilder<CottonClientCommandSource> itemCommand = ArgumentBuilders.literal("item");

		itemCommand.then(ArgumentBuilders.literal("name")
			.then(ArgumentBuilders.argument("name", TextArgumentType.text()).executes(ctx -> {

				Text name = ctx.getArgument("name", Text.class);

				name = FzmmUtils.replaceColorCodes(name);
				renameItem(name);
				return 1;
			}))
		);

		itemCommand.then(ArgumentBuilders.literal("lore")
			.then(ArgumentBuilders.literal("add")
				.then(ArgumentBuilders.argument("message", TextArgumentType.text()).executes(ctx -> {

					Text message = ctx.getArgument("message", Text.class);

					message = FzmmUtils.replaceColorCodes(message);
					addLore(message);
					return 1;
				}))
			).then(ArgumentBuilders.literal("addfromconfig")
				.then(ArgumentBuilders.argument("config key", StringArgumentType.word()).executes(ctx -> {

					addLoreFromConfig(ctx.getArgument("config key", String.class));
					return 1;
				}))
			).then(ArgumentBuilders.literal("remove")
				.then(ArgumentBuilders.argument("line", IntegerArgumentType.integer(0, 32767)).executes(ctx -> {

					removeLore(ctx.getArgument("line", int.class));
					return 1;
				}))
			)
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
			.then(ArgumentBuilders.argument("enchantment", EnchantmentArgumentType.enchantment()).executes(ctx -> {

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

		itemCommand.then(ArgumentBuilders.literal("overstack")
			.then(ArgumentBuilders.argument("amount", IntegerArgumentType.integer(2, 127)).executes(ctx -> {

				int amount = ctx.getArgument("amount", int.class);
				overStack(amount);
				return 1;

			}))
		);

		itemCommand.then(ArgumentBuilders.literal("skull")
			.then(ArgumentBuilders.argument("skull owner", StringArgumentType.greedyString()).suggests(FzmmUtils.SUGGESTION_PLAYER)
				.executes(ctx -> {

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
				key = FzmmUtils.replaceColorCodes(new LiteralText(key)).getString();
				lockContainer(key);
				return 1;

			}))
		);

		return itemCommand;
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

		ItemStack stack = MC.player.getInventory().getMainHandStack();
		stack.addEnchantment(enchant, level);
		FzmmUtils.giveItem(stack);
	}

	private static void displayNbt() {
		assert MC.player != null;
		ItemStack stack = MC.player.getInventory().getMainHandStack();

		if (stack.getTag() == null) {
			throw ERROR_WITHOUT_NBT;
		}
		String nbt = stack.getTag().toString().replaceAll("§", "\u00a7");

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

	private static void overStack(int amount) {
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

		itemStack.setTag(tag);
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

		if (!(containerItemStack.getTag() == null)) {
			tag = containerItemStack.getTag();

			if (!(containerItemStack.getTag().getCompound("BlockEntityTag") == null)) {
				items = fillSlots(tag.getCompound("BlockEntityTag").getList("Items", 10), itemStack, slotsToFill, firstSlots);
				blockEntityTag.put("Items", items);
			}
		}

		tag.put("BlockEntityTag", blockEntityTag);
		containerItemStack.setTag(tag);
		FzmmUtils.giveItem(containerItemStack);
	}

	private static NbtList fillSlots(NbtList nbtList, ItemStack itemStack, int slotsToFill, int firstSlot) {
		for (int i = 0; i != slotsToFill; i++) {
			NbtCompound tagItems = new NbtCompound();

			tagItems.putInt("Slot", i + firstSlot);
			tagItems.putString("id", itemStack.getItem().toString());
			tagItems.putInt("Count", itemStack.getCount());
			if (!(itemStack.getTag() == null)) tagItems.put("tag", itemStack.getTag());

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

		if (!(containerItemStack.getTag() == null)) {
			tag = containerItemStack.getTag();

			if (!(containerItemStack.getTag().getCompound("BlockEntityTag") == null))
				tag.getCompound("BlockEntityTag").putString("Lock", key);
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

		display.put("Lore", lore);

		if (!(itemStack.getTag() == null)) {
			tag = itemStack.getTag();

			if (!(itemStack.getTag().getCompound("display") == null)) {
				lore = tag.getCompound("display").getList("Lore", 8);
				lore.add(NbtString.of(Text.Serializer.toJson(message)));
				display.put("Lore", lore);
				display.putString("Name", tag.getCompound("display").getString("Name"));
			}
		}

		tag.put("display", display);
		itemStack.setTag(tag);
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

			for (String lore: loreArrayString) {
				loreArray.add(NbtString.of(lore));
			}

			if (!configFound) {
				throw ERROR_CONFIG_NOT_FOUND;
			}

			itemStack.setTag(FzmmUtils.addLores(itemStack, loreArray));

			FzmmUtils.giveItem(itemStack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void removeLore(int lineToRemove) {
		assert MC.player != null;

		//{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

		ItemStack itemStack = MC.player.getMainHandStack();

		if (!(itemStack.getTag() == null)) {
			NbtCompound tag = itemStack.getTag();

			if (!(itemStack.getTag().getCompound("display") == null)) {
				NbtCompound display = new NbtCompound();
				NbtList lore;

				lore = tag.getCompound("display").getList("Lore", 8);
				lore.remove(lineToRemove);
				display.put("Lore", lore);
				display.putString("Name", tag.getCompound("display").getString("Name"));

				tag.put("display", display);
				itemStack.setTag(tag);
				FzmmUtils.giveItem(itemStack);
			}
		}

	}
}