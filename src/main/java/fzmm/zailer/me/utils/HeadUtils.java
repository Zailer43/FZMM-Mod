package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import io.wispforest.owo.Owo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//TODO: update to mineskin 2.0
public class HeadUtils {
    public static final String MINESKIN_API = "https://api.mineskin.org/";
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private String skinValue;
    private String signature;
    private String url;
    private boolean skinGenerated;
    private int httpResponseCode;
    private int delayForNextInMillis;

    public HeadUtils() {
        this.skinValue = "";
        this.signature = "";
        this.url = "";
        this.skinGenerated = false;
        this.httpResponseCode = 0;
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

    public int getHttpResponseCode() {
        return this.httpResponseCode;
    }

    public int getDelayForNext(TimeUnit unit) {
        return (int) unit.convert(this.delayForNextInMillis, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<HeadUtils> uploadHead(BufferedImage headSkin, String skinName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection conn = null;
            try {
                FzmmConfig.Mineskin config = FzmmClient.CONFIG.mineskin;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(headSkin, "png", baos);
                byte[] skin = baos.toByteArray();
                URL url = URI.create(MINESKIN_API + "generate/upload").toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent", FzmmClient.HTTP_USER_AGENT);
                conn.setRequestMethod("POST");
                if (!config.apiKey().isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + config.apiKey());
                }
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

                this.readResponse(skinName, conn);

            } catch (IOException e) {
                FzmmClient.LOGGER.error("[HeadUtils] Head '{}' could not be generated", skinName, e);
                this.skinValue = "";
                this.skinGenerated = false;
                if (this.httpResponseCode == 0) {
                    this.httpResponseCode = 400;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (!this.skinGenerated) {
                this.delayForNextInMillis = 6000;
            }

            return this;
        }, Util.getDownloadWorkerExecutor());
    }

    private void readResponse(String skinName, HttpURLConnection conn) throws IOException {
        this.httpResponseCode = conn.getResponseCode();
        boolean isSuccess = this.httpResponseCode / 100 == 2;
        try (InputStreamReader streamReader = new InputStreamReader(isSuccess ? conn.getInputStream() : conn.getErrorStream())) {
            StringBuilder stringBuilder = new StringBuilder();
            int character;
            while ((character = streamReader.read()) != -1) {
                stringBuilder.append((char) character);
            }
            String reply = stringBuilder.toString();
            if (Owo.DEBUG) {
                FzmmClient.LOGGER.info("[DEBUG] [HeadUtils] HTTP Code: {}, Delay: {}, Received response: {}", this.httpResponseCode, this.delayForNextInMillis, reply);
            }

            JsonObject json = (JsonObject) JsonParser.parseString(reply);

            if (isSuccess) {
                FzmmClient.LOGGER.info("[HeadUtils] '{}' head generated using mineskin", skinName);
                this.useSuccessResponse(json);
            } else {
                this.logErrorResponse(json, skinName);
            }
        } catch (NullPointerException e) {
            FzmmClient.LOGGER.error("[HeadUtils] Failed to get head values from mineskin api", e);
        }
    }

    private void useSuccessResponse(JsonObject json) {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        JsonObject texture = json.getAsJsonObject("data").getAsJsonObject("texture");
        this.skinValue = texture.get("value").getAsString();
        this.signature = texture.get("signature").getAsString();
        this.url = texture.get("url").getAsString();
        this.skinGenerated = true;
        this.delayForNextInMillis = json.getAsJsonObject("delayInfo").get("millis").getAsInt();
    }

    private void logErrorResponse(JsonObject json, String skinName) {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        String code = json.get("errorCode").getAsString();
        String error = json.get("error").getAsString();

        FzmmClient.LOGGER.error("[HeadUtils] HTTP error {}, generating skin '{}', Code: '{}', Error: '{}'", this.httpResponseCode, skinName, code, error);
    }

    public static Optional<BufferedImage> getSkin(ItemStack stack) throws IOException {
        NbtCompound skullOwnerTag = stack.getSubNbt(PlayerHeadItem.SKULL_OWNER_KEY);
        if (skullOwnerTag == null) {
            return Optional.empty();
        }
        GameProfile profile = NbtHelper.toGameProfile(skullOwnerTag);
        if (profile == null) {
            return Optional.empty();
        }

        return new CacheSkinGetter().getSkin(profile);
    }

    public static Optional<SkinTextures> getSkinTextures(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound skullOwnerTag = nbt.getCompound(PlayerHeadItem.SKULL_OWNER_KEY);
        GameProfile gameProfile = NbtHelper.toGameProfile(skullOwnerTag);

        if (gameProfile == null)
            return Optional.empty();

        return Optional.of(MinecraftClient.getInstance()
                .getSkinProvider()
                .getSkinTextures(gameProfile)
        );
    }

    public static Optional<ItemStack> uploadAndGetHead(String playerName) {
        Optional<BufferedImage> skinOptional = new CacheSkinGetter().getSkin(playerName);
        if (skinOptional.isEmpty()) {
            return Optional.empty();
        }

        ItemStack stack = null;
        try {
            stack = new HeadUtils().uploadHead(skinOptional.get(), playerName).get()
                    .getBuilder()
                    .headName(playerName)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            FzmmClient.LOGGER.error("[HeadUtils] Error uploading head in mineskin", e);
        }

        return Optional.ofNullable(stack);
    }

    public static GameProfile minimizeTextures(GameProfile profile) {
        GameProfile result = new GameProfile(profile.getId(), profile.getName());
        Optional<String> unwrappedUrl = unwrapUrl(profile);
        if (unwrappedUrl.isEmpty()) {
            return result;
        }

        Optional<String> wrappedUrl = wrapUrl(unwrappedUrl.get());
        if (wrappedUrl.isEmpty()) {
            return result;
        }

        result.getProperties().put("textures", new Property("textures", wrappedUrl.get()));

        return result;
    }

    public static Optional<String> unwrapUrl(GameProfile profile) {
        List<Property> texturesProperties = profile.getProperties().get("textures").stream().toList();

        if (texturesProperties.isEmpty()) {
            return Optional.empty();
        }

        Optional<String> textureValueOptional = FzmmUtils.decodeBase64(texturesProperties.get(0).value());
        if (textureValueOptional.isEmpty()) {
            return Optional.empty();
        }

        try {
            JsonObject json = JsonParser.parseString(textureValueOptional.get()).getAsJsonObject();
            if (!json.has("textures") || !json.get("textures").isJsonObject()) {
                return Optional.empty();
            }

            JsonObject textures = json.getAsJsonObject("textures");
            if (!textures.has("SKIN") || !textures.get("SKIN").isJsonObject()) {
                return Optional.empty();
            }

            json = textures.get("SKIN").getAsJsonObject();
            if (!json.has("url") || !json.get("url").isJsonPrimitive()) {
                return Optional.empty();
            }

            JsonPrimitive jsonUrl = json.getAsJsonPrimitive("url");
            if (!jsonUrl.isString()) {
                return Optional.empty();
            }

            return Optional.of(jsonUrl.getAsString());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static Optional<String> wrapUrl(String url) {
        JsonObject skin = new JsonObject();
        skin.addProperty("url", url);

        JsonObject textures = new JsonObject();
        textures.add("SKIN", skin);

        JsonObject json = new JsonObject();
        json.add("textures", textures);

        return FzmmUtils.encodeBase64(json.toString());
    }
}
