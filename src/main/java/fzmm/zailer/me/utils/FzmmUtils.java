package fzmm.zailer.me.utils;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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

	public static CompoundTag addLores(ItemStack itemStack, ArrayList<StringTag> loreArray) {
		CompoundTag tag = new CompoundTag();
		CompoundTag display = new CompoundTag();
		ListTag lore;

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

	public static String StringNumber(String text) {
		return text.replaceAll("[^\\d]", "");
	}

	//TODO: Metodo para givear items de manera segura al jugador
	// verificando si supera o no el límite de 1.9mb de nbt en su inv actual + el item que se va a givear
}