package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.config.Configs;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.*;
import net.minecraft.network.MessageType;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

        if (Configs.Generic.GIVE_CLIENT_SIDE.getBooleanValue()) {
            mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
            assert mc.interactionManager != null;
            PlayerInventory playerInventory = mc.player.getInventory();

            playerInventory.addPickBlock(stack);
            mc.interactionManager.clickCreativeStack(stack, 36 + playerInventory.selectedSlot);
        }
    }

    public static String getPlayerUuid(String name) throws IOException {
        InputStream response = httpGetRequest("https://api.mojang.com/users/profiles/minecraft/" + name, false);
        if (response == null)
            return "";
        JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(response));
        return obj.get("id").getAsString();
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    @Nullable
    public static BufferedImage getImageFromUrl(String urlLocation) throws IOException {
        InputStream response = httpGetRequest(urlLocation, true);
        if (response == null)
            return null;
        return ImageIO.read(response);
    }

    @Nullable
    public static InputStream httpGetRequest(String url, boolean isImage) throws IOException {
        InputStream inputResponse = null;
        HttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        if (isImage)
            httpGet.addHeader("content-type", "image/jpeg");

        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity resEntity = response.getEntity();
        if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
            inputResponse = resEntity.getContent();

        return inputResponse;
    }

    public static Text disableItalicConfig(Text message) {
        Style style = message.getStyle();

        if (Configs.Generic.DISABLE_ITALIC.getBooleanValue() && !style.isItalic()) {
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

    public static String getNbtLengthInKB(NbtCompound nbt) {
        return new DecimalFormat("#,##0.0").format(getNbtLength(nbt) / 1024f) + "KB";
    }

    public static int getNbtLength(NbtCompound nbt) {
        return nbt.asString().length();
    }

    public static NbtString stringToNbtString(String string, boolean useDisableItalicConfig) {
        Text text = new LiteralText(string);
        return textToNbtString(text, useDisableItalicConfig);
    }

    public static NbtString textToNbtString(Text text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig)
            disableItalicConfig(text);
        return NbtString.of(Text.Serializer.toJson(text));
    }

    public static ItemStack getPlayerHead(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        head.setSubNbt(SkullItem.SKULL_OWNER_KEY, NbtString.of(username));

        return head;
    }

    public static ItemStack getPlayerHead(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound skullOwner = new NbtCompound();

        NbtHelper.writeGameProfile(skullOwner, profile);
        head.setSubNbt(SkullItem.SKULL_OWNER_KEY, skullOwner);

        return head;
    }

    public static void renameHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = mc.player.getInventory().getMainHandStack();
        stack.setCustomName(FzmmUtils.disableItalicConfig(text));
        FzmmUtils.giveItem(stack);
    }

    public static Color4f RGBAtoRGB(Color4f color) {
        return new Color4f(color.r, color.g, color.b, 0);
    }
}