package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class ImagetextLogic {
    private List<Component> imagetext = new ArrayList<>();
    private long textLength = 0L;
    private int width = 0;
    private int height = 0;

    public void buildImagetext(IImagetextAlgorithm algorithm, ImagetextData data) {
        this.width = data.width();
        this.height = data.height();
        this.textLength = 0L;
        this.imagetext = this.build(algorithm, data);
    }

    public List<Component> build(IImagetextAlgorithm algorithm, ImagetextData data) {
        algorithm.tryUpdateCache(data);
        algorithm.build();
        List<Component> result = new ArrayList<>(data.height());

        ImagetextLine line = new ImagetextLine(data.similarityThreshold());
        for (int y = 0; y != data.height(); y++) {
            line.characters(algorithm.linePixels(y));
            for (int x = 0; x != data.width(); x++) {
                line.add(algorithm.colorAt(x, y));
            }

            result.add(line.build());
            this.textLength += line.textLength();
            line.reset();
        }

        return result;
    }

    /**
     * Adjusts the size of an image to preserve the aspect ratio when changing either width or height.
     *
     * @param value          The original dimension value (width or height).
     * @param changeValue    The new dimension value (width or height) to maintain the aspect ratio.
     * @param referenceValue The dimension value that corresponds to the original reference (width or height).
     * @return int           The new dimension value that maintains the aspect ratio.
     */
    public static int getResizedAspectRatio(int value, int changeValue, int referenceValue) {
        return (int) ((double) changeValue / value * referenceValue);
    }

    public void addResolution() {
        String message = Component.translatable("fzmm.item.imagetext.resolution", this.width, this.height).getString();
        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();
        MutableComponent text = Component.translatable(message).setStyle(Style.EMPTY.withColor(color));
        this.imagetext.add(FzmmUtils.disableItalicConfig(text, true));
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public List<Component> text() {
        return this.imagetext;
    }

    public long textLength() {
        return this.textLength;
    }

    public Component mergeText() {
        return TextUtils.mergeText(this.text());
    }

    public boolean isEmpty() {
        return this.imagetext.isEmpty();
    }
}