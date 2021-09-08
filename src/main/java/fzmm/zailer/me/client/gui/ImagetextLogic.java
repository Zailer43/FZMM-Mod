package fzmm.zailer.me.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.LoreUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ImagetextLogic {
	public static void addLoreImagetext(BufferedImage image, int width) {
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
		if (itemStack.isEmpty())
			itemStack = Items.PAPER.getDefaultStack();

		ArrayList<NbtString> loreArray = generateImagetext(resizeImage(image, width));

		itemStack.setNbt(LoreUtils.createMultipleLore(itemStack, loreArray));
		FzmmUtils.giveItem(itemStack);
	}

	public static void giveBookImagetext(BufferedImage image, int width, String bookAuthor, String bookText) {
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
		NbtCompound tag = new NbtCompound();
		NbtList pages = new NbtList();

		ArrayList<NbtString> loreArray = generateImagetext(resizeImage(image, width));
		StringBuilder hoverEventStringBuilder = new StringBuilder();

		for (NbtString loreLine : loreArray) {
			hoverEventStringBuilder.append(
				loreLine.asString().
					replaceAll("\"}]$", "\\\\n\"},")
					.replaceAll("^\\[", ""));
		}
		String hoverEventStringContents = "[" + hoverEventStringBuilder.toString().replaceAll(",$", "") + "]";

		JsonParser parser = new JsonParser();
		JsonObject jsonHoverEvent = new JsonObject();
		jsonHoverEvent.add("action", parser.parse("show_text"));
		jsonHoverEvent.add("contents", parser.parse(hoverEventStringContents));

		pages.add(NbtString.of(Text.Serializer.toJson(
			new LiteralText(Formatting.BLUE + bookText)
				.setStyle(Style.EMPTY
					.withHoverEvent(HoverEvent.fromJson(jsonHoverEvent)))
		)));

		tag.putString(WrittenBookItem.TITLE_KEY, "Imagebook");
		tag.putString(WrittenBookItem.AUTHOR_KEY, bookAuthor);
		tag.put(WrittenBookItem.PAGES_KEY, pages);

		itemStack.setNbt(tag);
		assert itemStack.getNbt() != null;
		if (itemStack.getNbt().toString().length() > 32500) {
			ImagetextScreen.bookNbtTooLong = true;
		} else {
			FzmmUtils.giveItem(itemStack);
		}
	}

	@Nullable
	public static BufferedImage getImageFromUrl(String urlLocation) {
		BufferedImage image = null;
		try {
			image = FzmmUtils.getImageFromUrl(urlLocation);
		} catch (IOException e) {
			e.printStackTrace();
			ImagetextScreen.errorImage = true;
			ImagetextScreen.errorImageMessage = new TranslatableText("imagetext.error.malformedUrl");
		}

		return image;
	}

	public static BufferedImage getImageFromPc(String filePath) throws IOException {
		BufferedImage image = ImageIO.read(new URL("file:///" + filePath));
		if (image == null)
			throw new IOException();
		return image;
	}

	private static BufferedImage resizeImage(BufferedImage img, int newW) {
		if (img.getWidth() < newW)
			return img;

		FzmmConfig.Imagetext config = FzmmConfig.get().imagetext;
		int newH = Math.round(((float) (newW) / img.getWidth()) * img.getHeight());
		Image tmp = img.getScaledInstance(newW, newH, config.imagetextScale.value);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	private static ArrayList<NbtString> generateImagetext(BufferedImage image) {
		ArrayList<NbtString> loreArray = new ArrayList<>();
		int height = image.getHeight(),
			width = image.getWidth();
		final String PIXEL_CHARACTER = ImagetextScreen.pixelTextField.getText().matches("[\"'\\\\]") ? "█" : ImagetextScreen.pixelTextField.getText();

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
						.replaceAll(FzmmUtils.escapeSpecialRegexChars("", PIXEL_CHARACTER, "\"}$"), PIXEL_CHARACTER + PIXEL_CHARACTER + "\"}");
					loreLine.set(lastLore, modifiedLore);
				} else {
					loreLine.add(Text.Serializer.toJson(
						new LiteralText(PIXEL_CHARACTER).setStyle(
							Style.EMPTY.withColor(TextColor.fromRgb(pixelRGB)).withItalic(false)
						)));
				}
			}
			loreArray.add(NbtString.of(String.valueOf(loreLine)));
		}
		if (ImagetextScreen.showResolutionCheckbox.isChecked()) {
			loreArray.add(NbtString.of(String.valueOf(
				Text.Serializer.toJson(FzmmUtils.disableItalicConfig(
					new LiteralText("Resolution: " + width + "x" + height).setStyle(
						Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.GREEN))
					))))));
		}
		return loreArray;
	}
}