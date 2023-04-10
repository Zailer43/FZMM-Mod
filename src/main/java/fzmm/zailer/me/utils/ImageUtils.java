package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.headGenerator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.headGenerator.model.HeadModelEntry;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

public class ImageUtils {
    private static final Identifier OLD_FORMAT_TO_NEW_FORMAT_IDENTIFIER = new Identifier(FzmmClient.MOD_ID, HeadResourcesLoader.FZMM_MODELS_FOLDER + "/skins/old_format_to_new_format.json");
    public static final HeadModelEntry OLD_FORMAT_TO_NEW_FORMAT;

    static {
        OLD_FORMAT_TO_NEW_FORMAT = getOldFormatToNewFormatEntry().orElseGet(() -> {
          FzmmClient.LOGGER.error("Error loading ImageUtils.OLD_FORMAT_TO_NEW_FORMAT");
          return new HeadModelEntry("", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        });
    }


    public static Optional<BufferedImage> getBufferedImgFromIdentifier(Identifier identifier) {
        try {
            Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            return imageResource.isEmpty() ? Optional.empty() : Optional.of(ImageIO.read(imageResource.get().getInputStream()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static BufferedImage getBufferedImgFromNativeImg(NativeImage nativeImage) {
        int width = nativeImage.getWidth();
        int height = nativeImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = nativeImage.getColor(x, y);//ABGR (wtff?????)

                bufferedImage.setRGB(x, y, ((color >> 16) & 0xFF) | ((color & 0xFF) << 16) | (color & 0xFF00FF00));//ARGB
            }
        }

        return bufferedImage;
    }


    public static Optional<BufferedImage> getPlayerSkin(String name) throws NullPointerException, JsonIOException, IOException {
        GetSkinDecorator getSkinDecorator = new GetSkinFromCache(new GetSkinFromMojang());
        Optional<BufferedImage> skin = getSkinDecorator.getSkin(name);

        if (skin.isEmpty())
            FzmmClient.LOGGER.warn("[ImageUtils] skin of '{}' was not found", name);

        return skin;
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    public static Optional<BufferedImage> getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return Optional.ofNullable(ImageIO.read(url));
    }

    public static Optional<NativeImage> toNativeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            byte[] bytes = stream.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            return Optional.of(NativeImage.read(data));
        } catch (IOException ignored) {
            FzmmClient.LOGGER.error("[ImageUtils]: could not convert BufferedImage to NativeImage");
            return Optional.empty();
        }
    }

    public static boolean isAlexModel(int scale, BufferedImage skin) {
        int color = skin.getRGB((SkinPart.LEFT_ARM.x() + 15) * scale, (SkinPart.LEFT_ARM.y() + 15) * scale);
        int alpha = new Color(color, true).getAlpha();
        return alpha == 0;
    }

    public static BufferedImage convertInSteveModel(BufferedImage skin, int scale) {
        BufferedImage modifiedSkin = convertInSteveModel(skin, SkinPart.LEFT_ARM, scale);
        return convertInSteveModel(modifiedSkin, SkinPart.RIGHT_ARM, scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage playerSkin, SkinPart skinPart, int scale) {
        BufferedImage modifiedSkin = convertInSteveModel(playerSkin, skinPart.x(), skinPart.y(), scale);
        return convertInSteveModel(modifiedSkin, skinPart.hatX(), skinPart.hatY(), scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage skin, int x, int y, int scale) {
        x *= scale;
        y *= scale;
        int imageSize = 64 * scale;
        int space = 4 * scale;
        int steveArmWidth = 4 * scale;
        int alexArmWidth = 3 * scale;
        int skinPartSize = 16 * scale;
        BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        // copy skin
        g2d.drawImage(skin, 0, 0, imageSize, imageSize, 0, 0, imageSize, imageSize, null);
        // clear skin part
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(x, y, skinPartSize, skinPartSize);
        // copy side 1
        g2d.drawImage(skin, x, y + space, x + steveArmWidth, y + skinPartSize, x, y + space, x + steveArmWidth, y + skinPartSize, null);
        // stretching face 2
        g2d.drawImage(skin, x + steveArmWidth, y + space, x + steveArmWidth * 2, y + skinPartSize, x + steveArmWidth, y + space, x + steveArmWidth + alexArmWidth, y + skinPartSize, null);
        // moving face 3
        g2d.drawImage(skin, x + steveArmWidth * 2, y + space, x + steveArmWidth * 3, y + skinPartSize, x + steveArmWidth + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth, y + skinPartSize, null);
        // stretching and moving face 4
        g2d.drawImage(skin, x + steveArmWidth * 3, y + space, x + steveArmWidth * 4, y + skinPartSize, x + steveArmWidth * 2 + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth * 2, y + skinPartSize, null);
        // stretching top/down face 1
        g2d.drawImage(skin, x + space, y, x + steveArmWidth + space, y + space, x + space, y, x + alexArmWidth + space, y + space, null);
        // stretching and moving top/down face 2
        g2d.drawImage(skin, x + space + steveArmWidth, y, x + steveArmWidth * 2 + space, y + space, x + space + alexArmWidth, y, x + alexArmWidth * 2 + space, y + space, null);

        g2d.dispose();
        return bufferedImage;
    }


    private static Optional<HeadModelEntry> getOldFormatToNewFormatEntry() {
        Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(OLD_FORMAT_TO_NEW_FORMAT_IDENTIFIER);
        if (imageResource.isEmpty())
            return Optional.empty();

        Resource resource = imageResource.get();

        try {
            InputStream inputStream = resource.getInputStream();
            HeadModelEntry result = HeadResourcesLoader.getHeadModel(OLD_FORMAT_TO_NEW_FORMAT_IDENTIFIER, inputStream);
            inputStream.close();
            return Optional.of(result);
        } catch (IOException e) {
            FzmmClient.LOGGER.error("Error loading head generator '{}' model", OLD_FORMAT_TO_NEW_FORMAT, e);
        }

        return Optional.empty();
    }
}
