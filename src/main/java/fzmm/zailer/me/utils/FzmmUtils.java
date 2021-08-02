package fzmm.zailer.me.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.config.FzmmConfig;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.MessageType;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
        String color = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().general.loreColorPickBlock;
        color = color.replaceAll("[^0-9A-Fa-f]]", "");
        if (color.length() != 6) {
            color = "19b2ff";
        }
        return NbtString.of(Text.Serializer.toJson(new LiteralText(message).setStyle(
                Style.EMPTY.withColor(Integer.valueOf(color, 16))
                        .withItalic(false)
        )));
    }


    public static void giveItem(ItemStack stack) {
        boolean exceedLimit = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        if (stack.getTag() != null) {
            NbtCompound tag = stack.getTag();

            // FIXME: MC-86153
            //  No funciona cuando se tiene que recibir el paquete del NBT de los blockEntity
            if (tag.asString().length() > 1950000) {
                exceedLimit = true;
            }
        }

        if (exceedLimit) {
            mc.inGameHud.addChatMessage(MessageType.SYSTEM, new TranslatableText("giveitem.exceedLimit").setStyle(Style.EMPTY.withColor(Formatting.RED)), mc.player.getUuid());
        } else if (AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().general.giveClientSideItem) {
            mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else if (mc.player.isCreative()) {
            assert mc.interactionManager != null;
            PlayerInventory playerInventory = mc.player.getInventory();

            playerInventory.addPickBlock(stack);
            mc.interactionManager.clickCreativeStack(stack, 36 + playerInventory.selectedSlot);
        }
    }

    public static String getPlayerUuid(String name) throws IOException, ClassCastException {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JsonParser parser = new JsonParser();
        JsonObject obj = (JsonObject) parser.parse(new InputStreamReader(conn.getInputStream()));
        JsonElement uuid = obj.get("id");

        conn.disconnect();

        return uuid.getAsString();
    }

    public static BufferedImage getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        conn.connect();
        InputStream urlStream = conn.getInputStream();
        return ImageIO.read(urlStream);
    }
}