package me.zailer.testmod.client.test_command;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileDialogSource;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SkinPart;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.awt.image.BufferedImage;
import java.io.File;

import java.util.function.Predicate;

public class HeadGeneratorTest {

    public static void writeSkins() {
        new ImageFileDialogSource().execute(skin -> {
            boolean hasUnusedPixels = ImageUtils.hasUnusedPixel(skin);
            for (var model : HeadResourcesLoader.getLoaded()) {
                var result = getHead(model, skin, hasUnusedPixels);

                var file = new File(HeadGeneratorScreen.SKIN_SAVE_FOLDER_PATH + "/test/" + model.getKey() + ".png");
                if (file.mkdirs()) {
                    FzmmClient.LOGGER.info("[HeadGeneratorTest] Test skin save folder created");
                }

                HeadComponentOverlay.saveSkin(result, file);
            }
        });
    }

    public static void time(int loops) {
        new ImageFileDialogSource().execute(skin -> {
            var start = Util.getMeasuringTimeMs();

            var sum = 0L;
            var modelList = HeadResourcesLoader.getLoaded();
            var hashMap = new Object2LongOpenHashMap<String>(modelList.size());
            boolean hasUnusedPixels = ImageUtils.hasUnusedPixel(skin);

            for (int i = 0; i != loops; i++) {
                var loopStart = Util.getMeasuringTimeMs();
                for (var model : modelList) {
                    long modelTime = System.nanoTime();
                    var result = getHead(model, skin, hasUnusedPixels);
                    result.flush();
                    hashMap.addTo(model.getKey(), System.nanoTime() - modelTime);
                }
                sum += Util.getMeasuringTimeMs() - loopStart;
            }

            long totalTime = Util.getMeasuringTimeMs() - start;
            String message = "Time: " + "total " + totalTime + "ms / avg " + (sum / (float) loops) + "ms";

            var topEntries = hashMap.object2LongEntrySet()
                    .stream()
                    .sorted((e1, e2) -> Long.compare(e2.getLongValue(), e1.getLongValue()))
                    .limit(10)
                    .map(entry -> {
                        float modelTotal = entry.getLongValue() / 1000000f;
                        return entry.getKey() + ": " + String.format("%.2f", modelTotal / (float) totalTime * 100f)
                                + "% (" + modelTotal + "ms)";
                    })
                    .toList();

            String tooltip = String.join("\n", topEntries);
            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(Text.literal(message)
                            .setStyle(Style.EMPTY.withHoverEvent(
                                            new HoverEvent.ShowText(Text.literal(tooltip))
                                    )
                            )
                    );
        });
    }

    public static void checkFormat(boolean isSlim) {
        checkSkin(bufferedImage -> ImageUtils.isSlimFullCheck(bufferedImage) == isSlim, true);
    }

    public static void checkPixel(int x, int y, boolean subtractEmptyBody) {
        checkSkin(bufferedImage -> ImageUtils.hasPixel(x, y, bufferedImage), subtractEmptyBody);
    }

    public static void checkSkin(Predicate<BufferedImage> predicate, boolean subtractEmptyBody) {
        new ImageFileDialogSource().execute(skin -> {
            var entries = HeadResourcesLoader.getLoaded();
            var count = entries.size();
            var correctCount = 0;
            var missingBodyCount = 0;
            boolean hasUnusedPixels = ImageUtils.hasUnusedPixel(skin);
            for (var model : entries) {
                var headSkin = getHead(model, skin, hasUnusedPixels);
                var isEmptyBody = !ImageUtils.hasPixel(SkinPart.BODY.x() + 4, SkinPart.BODY.y() + 4, headSkin);

                if (isEmptyBody) {
                    missingBodyCount++;
                    if (subtractEmptyBody) {
                        continue;
                    }
                }

                if (predicate.test(headSkin)) {
                    correctCount++;
                } else {
                    FzmmClient.LOGGER.warn("[HeadGeneratorTest] Pixel not found in model '{}'", model.getKey());
                }
            }

            var totalCount = count;
            if (subtractEmptyBody) {
                totalCount -= missingBodyCount;
            }

            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(Text.literal("Correct: " + correctCount + "/" + totalCount
                            + " - Missing body: " + missingBodyCount + "/" + count
                    ));
        });
    }

    private static BufferedImage getHead(AbstractHeadEntry model, BufferedImage skin, boolean hasUnusedPixels) {
        var skinCopy = new BufferedImage(skin.getWidth(), skin.getHeight(), BufferedImage.TYPE_INT_ARGB);
        skinCopy.getGraphics().drawImage(skin, 0, 0, null);

        if (model instanceof HeadModelEntry modelEntry) {
            for (var textureParam : modelEntry.getNestedTextureParameters().parameterList()) {
                if (textureParam.isRequested()) {
                    textureParam.setValue(skin);
                }
            }
        }

        return model.getHeadSkin(skinCopy, hasUnusedPixels);
    }
}
