package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.Configs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Random;

public class HeadUtils {
    public static final String MINESKIN_API = "https://api.mineskin.org/";
    private static final Identifier HEADS_WATER_MARK = new Identifier(FzmmClient.MOD_ID, "textures/watermark/heads_watermark.png");
    private static final Logger LOGGER = LogManager.getLogger("FZMM HeadUtils");
    private String skinValue;
    private boolean skinGenerated;
    private int delayForNextInMillis;

    public HeadUtils() {
        this.skinValue = "";
        this.skinGenerated = false;
        this.delayForNextInMillis = 6000;
    }

    public ItemStack getHead() {
        return playerHeadFromSkin(this.skinValue);
    }

    public ItemStack getHead(String name) {
        ItemStack head = playerHeadFromSkin(this.skinValue);

        assert head.getNbt() != null;
        NbtCompound nbt = head.getNbt();
        if (!nbt.contains(SkullItem.SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE))
            return head;

        NbtCompound skullOwner = nbt.getCompound(SkullItem.SKULL_OWNER_KEY);
        skullOwner.putString("Name", name);

        return head;
    }

    public String getSkinValue() {
        return this.skinValue;
    }

    public boolean isSkinGenerated() {
        return this.skinGenerated;
    }

    public int getDelayForNextInMillis() {
        return this.delayForNextInMillis;
    }

    public HeadUtils uploadHead(BufferedImage headSkin, String skinName) throws IOException {
        this.applyWatermark(headSkin);
        String apiKey = Configs.Generic.MINESKIN_API_KEY.getStringValue();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(headSkin, "png", baos);
        byte[] skin = baos.toByteArray();

        try (CloseableHttpClient httpclient = HttpClients.custom().setUserAgent("FZMM/1.0").build()) {
            HttpPost httppost = new HttpPost(MINESKIN_API + "generate/upload");
            boolean isPublic = Configs.Generic.MINESKIN_PUBLIC_SKINS.getBooleanValue();

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("key", new StringBody(apiKey, ContentType.TEXT_PLAIN))
                    .addPart("visibility", new StringBody(isPublic ? "0" : "1", ContentType.TEXT_PLAIN))
                    .addBinaryBody("file", skin, ContentType.APPLICATION_FORM_URLENCODED, "head")
                    .build();

            httppost.setEntity(reqEntity);

            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                HttpEntity resEntity = response.getEntity();
                int httpCode = response.getStatusLine().getStatusCode();
                if (httpCode == HttpURLConnection.HTTP_OK) {
                    this.useResponse(EntityUtils.toString(resEntity));
                } else {
                    LOGGER.warn("HTTP error " + httpCode + " generating skin in '" + skinName + "'");
                    this.delayForNextInMillis = 6000;
                }
                EntityUtils.consume(resEntity);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    private void useResponse(String reply) throws IOException, InterruptedException {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        JsonObject json = (JsonObject) JsonParser.parseString(reply);
        this.skinValue = json.getAsJsonObject("data").getAsJsonObject("texture").get("value").getAsString();
        this.skinGenerated = true;
        this.delayForNextInMillis = (short) this.getDelay(json.getAsJsonObject("delayInfo").get("millis").getAsInt());
    }

    private int getDelay(int delay) {
        return MathHelper.clamp(delay, 2000, 6000);
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

    public static ItemStack playerHeadFromSkin(String skinValue) {
        NbtList textures = new NbtList();
        NbtCompound value = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound tag = new NbtCompound();
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

    private void applyWatermark(BufferedImage headSkin) {
        BufferedImage watermark = FzmmUtils.getImageFromIdentifier(HEADS_WATER_MARK);

        Graphics2D g2d = headSkin.createGraphics();
        g2d.drawImage(watermark, 0, 16, 64, 64, 0, 16, 64, 64, null);
        g2d.dispose();
    }
}
