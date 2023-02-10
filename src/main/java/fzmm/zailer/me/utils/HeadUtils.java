package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HeadUtils {
    public static final String MINESKIN_API = "https://api.mineskin.org/";
    private static final Identifier HEADS_WATER_MARK = new Identifier(FzmmClient.MOD_ID, "textures/watermark/heads_watermark.png");
    private static final Logger LOGGER = LogManager.getLogger("FZMM HeadUtils");
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private String skinValue;
    private boolean skinGenerated;
    private int delayForNextInMillis;

    public HeadUtils() {
        this.skinValue = "";
        this.skinGenerated = false;
        this.delayForNextInMillis = 6000;
    }

    public HeadBuilder getBuilder() {
        return HeadBuilder.builder()
                .skinValue(this.skinValue);
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

    public CompletableFuture<HeadUtils> uploadHead(BufferedImage headSkin, String skinName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (this.shouldApplyWatermark(headSkin))
                    this.applyWatermark(headSkin);
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
                    }
                } else {
                    LOGGER.warn("HTTP error " + httpCode + " generating skin in '" + skinName + "'");
                    this.delayForNextInMillis = 6000;
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        this.skinValue = json.getAsJsonObject("data").getAsJsonObject("texture").get("value").getAsString();
        this.skinGenerated = true;
        this.delayForNextInMillis = (short) this.getDelay(json.getAsJsonObject("delayInfo").get("millis").getAsInt());
    }

    private int getDelay(int delay) {
        return MathHelper.clamp(delay, 2000, 6000);
    }

    private void applyWatermark(BufferedImage headSkin) {
        Optional<BufferedImage> optionalWatermark = ImageUtils.getImageFromIdentifier(HEADS_WATER_MARK);
        optionalWatermark.ifPresent(watermark -> {
            Graphics2D g2d = headSkin.createGraphics();
            g2d.drawImage(watermark, 0, 16, 64, 64, 0, 16, 64, 64, null);
            g2d.dispose();
        });
    }

    private boolean shouldApplyWatermark(BufferedImage headSkin) {
        int width = headSkin.getWidth();
        int height = headSkin.getHeight();
        if (height < 16)
            return false;

        for (int x = 0; x != width; x++) {
            for (int y = 16; y != height; y++) {
                int alpha = (headSkin.getRGB(x, y) >> 24) & 0xFF;
                if (alpha != 0)
                    return false;
            }
        }

        return true;
    }
}
