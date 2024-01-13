package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HeadUtils {
    public static final String MINESKIN_API = "https://api.mineskin.org/";
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private String skinValue;
    private String signature;
    private String url;
    private boolean skinGenerated;
    private int delayForNextInMillis;

    public HeadUtils() {
        this.skinValue = "";
        this.signature = "";
        this.url = "";
        this.skinGenerated = false;
        this.delayForNextInMillis = 6000;
    }

    public HeadBuilder getBuilder() {
        return HeadBuilder.builder()
                .skinValue(this.skinValue)
                .signature(this.signature);
    }

    public String getSkinValue() {
        return this.skinValue;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isSkinGenerated() {
        return this.skinGenerated;
    }

    public int getDelayForNextInMillis() {
        return this.delayForNextInMillis;
    }

    public CompletableFuture<HeadUtils> uploadHead(BufferedImage headSkin, String skinName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FzmmConfig.Mineskin config = FzmmClient.CONFIG.mineskin;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(headSkin, "png", baos);
                byte[] skin = baos.toByteArray();

                URL url = new URL(MINESKIN_API + "generate/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestProperty("User-Agent", FzmmClient.HTTP_USER_AGENT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + config.apiKey());
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                try (DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream())) {
                    dataOutputStream.writeBytes("--" + BOUNDARY + "\r\n");
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"visibility\"\r\n");
                    dataOutputStream.writeBytes("Content-Type: text/plain\r\n\r\n");
                    dataOutputStream.writeBytes(config.publicSkins() ? "0" : "1");
                    dataOutputStream.writeBytes("\r\n--" + BOUNDARY + "\r\n");
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"head\"\r\n");
                    dataOutputStream.writeBytes("Content-Type: application/x-www-form-urlencoded\r\n\r\n");
                    dataOutputStream.write(skin);
                    dataOutputStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
                }

                int httpCode = conn.getResponseCode();
                if (httpCode / 100 == 2) {
                    try (InputStreamReader isr = new InputStreamReader(conn.getInputStream())) {
                        StringBuilder sb = new StringBuilder();
                        int ch;
                        while ((ch = isr.read()) != -1) {
                            sb.append((char) ch);
                        }
                        this.useResponse(sb.toString());
                        FzmmClient.LOGGER.info("[HeadUtils] '{}' head generated using mineskin", skinName);
                    }
                } else {
                    FzmmClient.LOGGER.error("[HeadUtils] HTTP error {} generating skin in '{}'", httpCode, skinName);
                    this.delayForNextInMillis = 6000;
                }
            } catch (IOException e) {
                FzmmClient.LOGGER.error("Head '{}' could not be generated", skinName, e);
                this.skinValue = "";
                this.skinGenerated = false;
                this.delayForNextInMillis = 6000;
            }
            return this;
        });
    }

    private void useResponse(String reply) {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        JsonObject json = (JsonObject) JsonParser.parseString(reply);
        JsonObject texture = json.getAsJsonObject("data").getAsJsonObject("texture");
        this.skinValue = texture.get("value").getAsString();
        this.signature = texture.get("signature").getAsString();
        this.url = texture.get("url").getAsString();
        this.skinGenerated = true;
        this.delayForNextInMillis = (short) this.getDelay(json.getAsJsonObject("delayInfo").get("millis").getAsInt());
    }

    private int getDelay(int delay) {
        return MathHelper.clamp(delay, 2000, 6000);
    }

    public static Optional<BufferedImage> getSkin(ItemStack stack) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound skullOwnerTag = nbt.getCompound(PlayerHeadItem.SKULL_OWNER_KEY);
        GameProfile gameProfile = NbtHelper.toGameProfile(skullOwnerTag);
        if (gameProfile == null)
            return Optional.empty();

        String textureUrl = MinecraftClient.getInstance()
                .getSkinProvider()
                .getSkinTextures(gameProfile)
                .textureUrl();

        return ImageUtils.getImageFromUrl(textureUrl);
    }
}
