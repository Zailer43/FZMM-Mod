package fzmm.zailer.me.client.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.github.cottonmc.clientcommands.ArgumentBuilders;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ImageTextCommand {
	static final CommandException IMAGE_NOT_FOUND = new CommandException(new TranslatableText("commands.fzmm.imagetext.imageNotFound"));
	static final CommandException MALFORMED_URL = new CommandException(new TranslatableText("commands.fzmm.imagetext.malformedUrl"));
	static final CommandException BOOK_LIMIT = new CommandException(new TranslatableText("commands.fzmm.imagetext.bookLimit"));
	static final int MAX_IMAGE_SIZE = 127;
	static final char PIXEL_CHARACTER = '█';
	static final String DEFAULT_BOOK_TEXT = "Pon el cursor encima de este mensaje para ver una imagen";

	//TODO: Arreglar mensaje de error diciendo que está incompleto el comando cuando
	// aparentemente no hubieron errores al ejecutar el comando
	public static LiteralArgumentBuilder<CottonClientCommandSource> getArgumentBuilder() {
		return ArgumentBuilders.literal("imagetext")
			.then(ArgumentBuilders.argument("Image URL", StringArgumentType.string())
				.then(ArgumentBuilders.argument("Width", IntegerArgumentType.integer(2, MAX_IMAGE_SIZE))
					.then(ArgumentBuilders.literal("addlore").executes(ctx -> {

						String url = ctx.getArgument("Image URL", String.class);
						int width = ctx.getArgument("Width", int.class);

						addLoreImageText(url, width);
						return 0;
					}))
					.then(ArgumentBuilders.literal("givebook").executes(ctx -> {
						String url = ctx.getArgument("Image URL", String.class);
						int width = ctx.getArgument("Width", int.class);

						giveBookImageText(url, width, DEFAULT_BOOK_TEXT);

						return 0;
					}).then(ArgumentBuilders.argument("Book text", StringArgumentType.string()).executes(ctx -> {
						String url = ctx.getArgument("Image URL", String.class),
							bookText = ctx.getArgument("Book text", String.class);
						int width = ctx.getArgument("Width", int.class);

						giveBookImageText(url, width, bookText);

						return 0;
					})))));
	}

	public static void addLoreImageText(String url, int width) {
		/*
		{
			display:{
				Lore:[
					'[{"text":"█","color":"#FF0080","italic":false},{"text":"██","color":"#E91080","italic":false}...]',
					'[{"text":"█","color":"#2086DE","italic":false},...]',
					...
		]}}
		*/
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;
		ItemStack itemStack = mc.player.getMainHandStack();

		ArrayList<StringTag> loreArray = generateImagetext(resizeImage(getImageFromUrl(url), width));

		itemStack.setTag(FzmmUtils.addLores(itemStack, loreArray));
		mc.player.equipStack(EquipmentSlot.MAINHAND, itemStack);
	}

	public static void giveBookImageText(String url, int width, String bookText) {
		/*
		{
			title:"Imagebook",
			author:"Zailer43",
			pages:[
				'{
					"text":"Pon el cursor encima de este mensaje para ver una imagen",
					"hoverEvent":{
						"action":"show_text",
						"contents":[
							{"text":"█","color":"#FF0080"},...{"text":"██\\n","color":"#E91080"},
							{"text":"█","color":"#2086de"},...
							...
				]}}'
		]}
		 */
		MinecraftClient mc = MinecraftClient.getInstance();
		assert mc.player != null;
		ItemStack itemStack = Items.WRITTEN_BOOK.getDefaultStack();
		CompoundTag tag = new CompoundTag();
		ListTag pages = new ListTag();

		ArrayList<StringTag> loreArray = generateImagetext(resizeImage(getImageFromUrl(url), width));
		StringBuilder hoverEventStringBuilder = new StringBuilder("");

		for (StringTag loreLine: loreArray) {
			hoverEventStringBuilder.append(
				loreLine.asString().
					replaceAll("\"}]$", "\\\\n\"},")
					.replaceAll("^\\[", ""));
		}
		String hoverEventStringContents = "[" + hoverEventStringBuilder.toString().replaceAll(",$", "") + "]";

		JsonParser parser=new JsonParser();
		JsonObject jsonHoverEvent = new JsonObject();
		jsonHoverEvent.add("action", parser.parse("show_text"));
		jsonHoverEvent.add("contents", parser.parse(hoverEventStringContents));

		pages.add(StringTag.of(Text.Serializer.toJson(
			new LiteralText(Formatting.BLUE + bookText)
			.setStyle(Style.EMPTY
				.withHoverEvent(HoverEvent.fromJson(jsonHoverEvent)))
			)));

		tag.putString("title", "Imagebook");
		tag.putString("author", mc.player.getName().getString());
		tag.put("pages", pages);

		itemStack.setTag(tag);
		if (itemStack.getTag().toString().length() > 32500)
			throw BOOK_LIMIT;
		mc.player.equipStack(EquipmentSlot.MAINHAND, itemStack);
	}

	public static BufferedImage getImageFromUrl(String urlLocation) {
		BufferedImage image;
		try {
			URL url = new URL(urlLocation);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");

			conn.connect();
			InputStream urlStream = conn.getInputStream();
			image = ImageIO.read(urlStream);

		} catch (IOException e) {
			e.printStackTrace();
			throw MALFORMED_URL;
		}

		if (image == null)
			throw IMAGE_NOT_FOUND;
		else
			return image;
	}

	public static BufferedImage resizeImage(BufferedImage img, int newW) {
		if (img.getWidth() < newW)
			return img;

		FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
		int newH = Math.round(((float) (newW) / img.getWidth()) * img.getHeight());
		Image tmp = img.getScaledInstance(newW, newH, config.general.imagetextScale.value);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	public static ArrayList<StringTag> generateImagetext(BufferedImage image) {
		ArrayList<StringTag> loreArray = new ArrayList<>();
		int height = image.getHeight(),
			width = image.getWidth();

		for (int y = 0; y != height; y++) {
			ArrayList<String> loreLine = new ArrayList<>();
			for (int x = 0; x != width; x++) {
				Color pixelARGB = new Color(image.getRGB(x, y));
				int pixelRGB = (pixelARGB.getRed() * 65536) +
					(pixelARGB.getGreen() * 256) +
					pixelARGB.getBlue();

				if (x > 0 && image.getRGB(x - 1, y) == image.getRGB(x, y)) {
					int lastLore = loreLine.size() - 1;
					String modifiedLore = loreLine.get(lastLore)
						.replaceFirst(String.valueOf(PIXEL_CHARACTER), String.valueOf(PIXEL_CHARACTER) + PIXEL_CHARACTER);
					loreLine.set(lastLore, modifiedLore);
				} else {
					loreLine.add(Text.Serializer.toJson(
						new LiteralText(String.valueOf(PIXEL_CHARACTER)).setStyle(
							Style.EMPTY.withColor(TextColor.fromRgb(pixelRGB)).withItalic(false)
						)));
				}
			}
			loreArray.add(StringTag.of(String.valueOf(loreLine)));
		}
		return loreArray;
	}
}
