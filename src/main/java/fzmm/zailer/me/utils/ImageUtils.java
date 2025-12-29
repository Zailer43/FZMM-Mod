package fzmm.zailer.me.utils;

import com.mojang.blaze3d.platform.NativeImage;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.api.IApiRemote;
import fzmm.zailer.me.utils.skin.SkinGetterDecorator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class ImageUtils {

    public static BufferedImage getBufferedImgFromNativeImg(NativeImage nativeImage) {
        int width = nativeImage.getWidth();
        int height = nativeImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, nativeImage.getPixels());

        return bufferedImage;
    }


    public static Optional<BufferedImage> getPlayerSkin(String name, SkinGetterDecorator skinGetterDecorator) {
        Optional<BufferedImage> skin = skinGetterDecorator.getSkin(name);

        if (skin.isEmpty()) {
            FzmmClient.LOGGER.warn("[ImageUtils] skin of '{}' was not found", name);
        }

        return skin;
    }

    public static Optional<BufferedImage> getImageFromUrl(String url) throws IOException {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", IApiRemote.HTTP_USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (InputStream is = response.body()) {
                    return Optional.ofNullable(ImageIO.read(is));
                }
            }
        } catch (Exception e) {
            FzmmClient.LOGGER.warn("[ImageUtils] Failed to get image", e);
        }

        return Optional.empty();
    }

    public static NativeImage toNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                nativeImage.setPixel(x, y, image.getRGB(x, y));
            }
        }
        return nativeImage;
    }

    public static byte[] toByteArray(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[ImageUtils] Error writing image", e);
            return null;
        }
        return baos.toByteArray();
    }

    public static BufferedImage withType(BufferedImage image, int type) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), type);

        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        image.flush();
        return newImage;
    }

    public static boolean isEquals(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight())
            return false;

        for (int y = 0; y < image1.getHeight(); y++) {
            for (int x = 0; x < image1.getWidth(); x++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y))
                    return false;
            }
        }
        return true;
    }

    public static boolean hasUnusedPixel(BufferedImage skin) {
        for (var rectangle : SkinPart.EMPTY_AREAS) {
            if (hasAnyPixel(rectangle[0], rectangle[1], rectangle[2], rectangle[3], skin)) {
                return true;
            }
        }

        return false;
    }

    public static void copyUnusedPixels(BufferedImage skin, Graphics2D target) {
        for (var rect : SkinPart.EMPTY_AREAS) {
            target.drawImage(skin, rect[0], rect[1], rect[2], rect[3],
                    rect[0], rect[1], rect[2], rect[3],
                    null
            );
        }
    }

    public static void removeUnusedPixels(Graphics2D target) {
        target.setBackground(new Color(0, 0, 0, 0));
        for (var rect : SkinPart.EMPTY_AREAS) {
            target.clearRect(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]);
        }
    }

    public static void drawUsedPixels(BufferedImage skin, boolean skinHatLayer, Graphics2D target, boolean targetHatLayer, SkinPart skinPart) {
        int targetIndex = targetHatLayer ? 2 : 0;
        int skinIndex = skinHatLayer ? 2 : 0;
        byte[][] usedAreas = skinPart.usedAreas();
        for (int i = targetIndex; i != targetIndex + 2; i++) {
            byte[] targetArea = usedAreas[i];
            byte[] skinArea = usedAreas[skinIndex++];
            target.drawImage(skin,
                    targetArea[0], targetArea[1], targetArea[2], targetArea[3],
                    skinArea[0], skinArea[1], skinArea[2], skinArea[3],
                    null
            );
        }
    }

    public static void clearRect(Graphics2D target, byte[][] rectangle) {
        for (var rect : rectangle) {
            target.clearRect(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]);
        }
    }

    public static boolean isSlimSimpleCheck(BufferedImage skin) {
        return isSlimSimpleCheck(skin, 1);
    }

    public static boolean isSlimSimpleCheck(BufferedImage skin, int scale) {
        return !hasPixel(scale, SkinPart.LEFT_ARM.x() + 15, SkinPart.LEFT_ARM.y() + 15, skin);
    }

    public static boolean isSlimFullCheck(BufferedImage skin) {
        var formatRectangles = new int[]{
                // hand and shoulder
                SkinPart.LEFT_ARM.x() + 10,
                SkinPart.LEFT_ARM.y(),
                SkinPart.LEFT_ARM.x() + 10 + 1,
                SkinPart.LEFT_ARM.y() + 3,
                // arm
                SkinPart.LEFT_ARM.x() + 14,
                SkinPart.LEFT_ARM.y() + 4,
                SkinPart.LEFT_ARM.x() + 15,
                SkinPart.LEFT_ARM.y() + 15,
                // hand and shoulder
                SkinPart.RIGHT_ARM.x() + 10,
                SkinPart.RIGHT_ARM.y(),
                SkinPart.RIGHT_ARM.x() + 10 + 1,
                SkinPart.RIGHT_ARM.y() + 3,
                // arm
                SkinPart.RIGHT_ARM.x() + 14,
                SkinPart.RIGHT_ARM.y() + 4,
                SkinPart.RIGHT_ARM.x() + 15,
                SkinPart.RIGHT_ARM.y() + 15
        };

        for (int i = 0; i != formatRectangles.length; i += 4) {
            if (hasAnyPixel(formatRectangles[i], formatRectangles[i + 1], formatRectangles[i + 2], formatRectangles[i + 3], skin)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyPixel(int x, int y, int x2, int y2, BufferedImage skin) {
        for (int i = x; i != x2; i++) {
            for (int j = y; j != y2; j++) {
                if (hasPixel(i, j, skin)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasPixel(int scale, int x, int y, BufferedImage skin) {
        return hasPixel(x * scale, y * scale, skin);
    }

    public static boolean hasPixel(int x, int y, BufferedImage skin) {
        if (x >= skin.getWidth() || y >= skin.getHeight()) {
            return false;
        }

        int alpha = skin.getColorModel().getAlpha(skin.getRaster().getDataElements(x, y, null));
        return alpha != 0;
    }

    // TODO:
    //  add scale in head models and replace this with InternalModels.SLIM_TO_WIDE
    //  scale is necessary in player statue (because there are 128x128 skins)
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

    // === BEGIN OF 'FILTHY RICH CLIENT' CODE ===

    /*
     * https://github.com/romainguy/filthy-rich-clients/blob/master/Images/PictureScaler/src/PictureScaler.java
     *
     * Created on May 1, 2007, 5:03 PM
     *
     * Copyright (c) 2007, Sun Microsystems, Inc
     * All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     *
     *   * Redistributions of source code must retain the above copyright
     *     notice, this list of conditions and the following disclaimer.
     *   * Redistributions in binary form must reproduce the above
     *     copyright notice, this list of conditions and the following
     *     disclaimer in the documentation and/or other materials provided
     *     with the distribution.
     *   * Neither the name of the TimingFramework project nor the names of its
     *     contributors may be used to endorse or promote products derived
     *     from this software without specific prior written permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
     * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
     * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
     * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
     * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
     * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
     * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
     * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */

    /**
     * Convenience method that returns a scaled instance of the
     * provided BufferedImage.
     *
     * @param image               the original image to be scaled
     * @param targetWidth         the desired width of the scaled instance,
     *                            in pixels
     * @param targetHeight        the desired height of the scaled instance,
     *                            in pixels
     * @param progressiveBilinear if true, this method will use a multi-step
     *                            scaling technique that provides higher quality than the usual
     *                            one-step technique (only useful in down-scaling cases, where
     *                            targetWidth or targetHeight is
     *                            smaller than the original dimensions)
     * @return a scaled version of the original BufferedImage
     * @author Chet
     */
    public static BufferedImage fastResizeImage(BufferedImage image, int targetWidth,
                                                int targetHeight, boolean progressiveBilinear) {
        boolean isTranslucent = image.getTransparency() != Transparency.OPAQUE;
        int type = isTranslucent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resizedImage = image;
        BufferedImage scratchImage = null;
        Graphics2D g2d = null;
        int prevWidth = resizedImage.getWidth();
        int prevHeight = resizedImage.getHeight();
        boolean isUpscale = prevWidth < targetWidth || prevHeight < targetHeight;

        do {
            int[] dimensions = calculateDimensions(prevWidth, prevHeight, targetWidth, targetHeight, progressiveBilinear, isUpscale);
            int width = dimensions[0];
            int height = dimensions[1];

            if (scratchImage == null || isTranslucent) {
                scratchImage = new BufferedImage(width, height, type);

                if (g2d != null) {
                    g2d.dispose();
                }

                g2d = scratchImage.createGraphics();
            }

            drawResizedImage(g2d, resizedImage, prevWidth, prevHeight, width, height);
            prevWidth = width;
            prevHeight = height;
            resizedImage = scratchImage;
        } while (prevWidth != targetWidth || prevHeight != targetHeight);

        g2d.dispose();

        return resizeFinalImageIfNeeded(resizedImage, targetWidth, targetHeight, type);
    }

    private static int[] calculateDimensions(int width, int height, int targetWidth, int targetHeight,
                                             boolean progressiveBilinear, boolean isUpscale) {

        if (progressiveBilinear) {
            width = isUpscale ? Math.min(width * 2, targetWidth) : Math.max(width / 2, targetWidth);
            height = isUpscale ? Math.min(height * 2, targetHeight) : Math.max(height / 2, targetHeight);
        } else {
            width = targetWidth;
            height = targetHeight;
        }

        return new int[]{width, height};
    }

    private static void drawResizedImage(Graphics2D g2, BufferedImage src, int srcWidth, int srcHeight, int destWidth, int destHeight) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, destWidth, destHeight, 0, 0, srcWidth, srcHeight, null);
    }

    private static BufferedImage resizeFinalImageIfNeeded(BufferedImage image, int targetWidth, int targetHeight, int type) {
        if (targetWidth != image.getWidth() || targetHeight != image.getHeight()) {
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);
            Graphics2D g2 = resizedImage.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
            return resizedImage;
        }
        return image;
    }

    // === END OF 'FILTHY RICH CLIENT' CODE ===

}
