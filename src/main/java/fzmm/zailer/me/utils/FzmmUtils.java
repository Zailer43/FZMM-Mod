package fzmm.zailer.me.utils;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.config.FzmmConfig;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class FzmmUtils {

	public static final SuggestionProvider<CottonClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {

		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.world != null;

		IntegratedServer integratedServer = mc.getServer();
		if (integratedServer != null) {
			String[] players = integratedServer.getPlayerNames();
			for (String player : players) {
				builder.suggest(player);
			}
		} else {
			List<AbstractClientPlayerEntity> players = mc.world.getPlayers();

			for (AbstractClientPlayerEntity player : players) {
				builder.suggest(player.getName().getString());
			}
		}

		return CompletableFuture.completedFuture(builder.build());

	};

	public static Text replaceColorCodes(Text message) {
		String messageString = message.toString();
		if (messageString.contains("&")) {
			messageString = messageString.replaceAll("&", "§");
			messageString = messageString.replaceAll("§§", "&");
			message = new LiteralText(messageString);
		}
		return message;
	}

	public static NbtCompound addLores(ItemStack itemStack, ArrayList<NbtString> loreArray) {
		NbtCompound tag = new NbtCompound();
		NbtCompound display = new NbtCompound();
		NbtList lore;

		if (itemStack.getTag() == null) {
			display.put("Lore", null);
			tag.put("display", display);
			itemStack.setTag(tag);
		}

		tag = itemStack.getTag();
		lore = tag.getCompound("display").getList("Lore", 8);
		lore.addAll(loreArray);
		display.put("Lore", lore);
		display.putString("Name", tag.getCompound("display").getString("Name"));
		tag.put("display", display);

		return tag;
	}

	public static String escapeSpecialRegexChars(String regexInit, String specialRegexChar, String regexEnd) {
		Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^\\\\|]");
		return Pattern.compile(regexInit + SPECIAL_REGEX_CHARS.matcher(specialRegexChar).replaceAll("\\\\$0") + regexEnd).toString();
	}

	public static NbtString generateLoreMessage(String message) {
		return NbtString.of(Text.Serializer.toJson(new LiteralText(message).setStyle(
			Style.EMPTY.withColor(AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().general.loreColorPickBlock)
				.withItalic(false)
		)));
	}

	public static void defaultListToNbtList(Iterable<ItemStack> iterableList, NbtList nbtList) {
		iterableList.forEach((ItemStack stack) -> {
			NbtCompound slot = new NbtCompound();
			if (!stack.isEmpty()) {
				slot.putString("id", stack.getItem().toString());
				slot.putByte("Count", (byte) stack.getCount());
				slot.put("tag", stack.getTag() == null ? new NbtCompound() : stack.getTag());
			}
			nbtList.add(slot);
		});
	}

	//TODO: Metodo para givear items de manera segura al jugador
	// verificando si supera o no el límite de 1.9mb de nbt en su inv actual + el item que se va a givear
}