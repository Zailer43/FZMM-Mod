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

}