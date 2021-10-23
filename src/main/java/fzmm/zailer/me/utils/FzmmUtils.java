package fzmm.zailer.me.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class FzmmUtils {

    public static final SuggestionProvider<FabricClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {

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


    public static void giveItem(ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        if (stack.hasNbt()) {
            NbtCompound tag = stack.getNbt();
            assert tag != null;

            // FIXME: MC-86153
            //  No funciona cuando se tiene que recibir el paquete del NBT de los blockEntity
            if (getNbtLength(tag) > 1950000) {
                mc.inGameHud.addChatMessage(MessageType.SYSTEM, new TranslatableText("giveItem.exceedLimit").setStyle(Style.EMPTY.withColor(Formatting.RED)), mc.player.getUuid());
                return;
            }
        }

        if (FzmmConfig.get().general.giveClientSideItem) {
            mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
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

    public static BufferedImage getImageFromPath(String path) throws IOException {
        return getImageFromUrl("file:///" + path);
    }

    public static BufferedImage getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        conn.connect();
        InputStream urlStream = conn.getInputStream();
        return ImageIO.read(urlStream);
    }

    public static Text disableItalicConfig(Text message) {
        Style style = message.getStyle();

        if (FzmmConfig.get().general.disableItalic && !style.isItalic()) {
            ((MutableText) message).setStyle(style.withItalic(false));
        }

        return message;
    }

    public static ItemStack playerHeadFromSkin(String skinValue) {
        NbtList textures = new NbtList();
        NbtCompound value = new NbtCompound(),
                properties = new NbtCompound(),
                skullOwner = new NbtCompound(),
                tag = new NbtCompound();
        Random random = new Random(new Date().getTime());
        NbtIntArray id = new NbtIntArray(new int[]{random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE),
                random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE)});

        value.putString("Value", skinValue);
        textures.add(value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.put("Id", id);

        tag.put(SkullItem.SKULL_OWNER_KEY, skullOwner);

        ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();
        stack.setNbt(tag);
        return stack;
    }

    public static void addSlot(NbtList slotList, int count, String id, int slot, @Nullable NbtCompound tag) {
        slotList.add(getSlotTag(count, id, slot, tag));
    }

    public static NbtCompound getSlotTag(int count, String id, int slot, @Nullable NbtCompound tag) {
        NbtCompound slotTag = new NbtCompound();
        slotTag.putByte("Count", (byte) count);
        slotTag.putString("id", id);
        slotTag.putByte("Slot", (byte) slot);
        if (tag != null)
            slotTag.put("tag", tag);
        return slotTag;
    }

    public static String getNbtLengthInKB(NbtCompound nbt) {
        return new DecimalFormat("#,##0.0").format(getNbtLength(nbt) / 1024f) + "KB";
    }

    public static int getNbtLength(NbtCompound nbt) {
        return nbt.asString().length();
    }
}