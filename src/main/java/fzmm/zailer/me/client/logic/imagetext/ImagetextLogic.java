package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextLogic {
    private NbtList imagetext;
    private int width;
    private int height;
    private String characters;

    public ImagetextLogic() {
        this.imagetext = new NbtList();
        this.width = 0;
        this.height = 0;
        this.characters = ImagetextLine.DEFAULT_TEXT;
    }

    public void generateImagetext(ImagetextData data) {
        this.generateImagetext(data, Integer.MAX_VALUE);
    }

    public void generateImagetext(ImagetextData data, int lineSplitInterval) {
        BufferedImage image = this.resizeImage(data.image(), data.width(), data.height(), data.smoothRescaling());
        this.width = data.width();
        this.height = data.height();
        this.characters = data.characters();
        if (this.characters == null || this.characters.isBlank())
            this.characters = ImagetextLine.DEFAULT_TEXT;
        NbtList linesList = new NbtList();

        for (int y = 0; y != height; y++) {
            ImagetextLine line = new ImagetextLine(this.characters, data.percentageOfSimilarityToCompress(), lineSplitInterval);
            for (int x = 0; x != width; x++) {
                line.add(image.getRGB(x, y));
            }

            for (var lineText : line.getLine())
                linesList.add(FzmmUtils.toNbtString(lineText, false));
        }
        this.imagetext = linesList;
    }

    private BufferedImage resizeImage(BufferedImage image, int width, int height, boolean smoothRescaling) {
        Image tmp = image.getScaledInstance(width, height, smoothRescaling ? Image.SCALE_SMOOTH : Image.SCALE_REPLICATE);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * @param width                Width of which you want to preserve the aspect ratio.
     * @param height               Height of which you want to preserve the aspect ratio.
     * @param referenceSide        The side that is used as a reference for the new resolution.
     * @param referenceSideIsWidth If the variable referenceSide is width (true) otherwise it is height (false).
     * @return Vec2f.x = width, Vec2f.y = height
     * <p>
     */
    public static Vec2f changeResolutionKeepingAspectRatio(int width, int height, int referenceSide, boolean referenceSideIsWidth) {
        int modifiedSide = (int) ((double) referenceSide / (referenceSideIsWidth ? width : height) * (referenceSideIsWidth ? height : width));

        return referenceSideIsWidth ? new Vec2f(referenceSide, modifiedSide) : new Vec2f(modifiedSide, referenceSide);
    }

    public void addResolution() {
        String message = Text.translatable("fzmm.item.imagetext.resolution", this.width, this.height).getString();
        int color = Integer.parseInt(FzmmClient.CONFIG.colors.imagetextMessages(), 16);
        Text text = Text.translatable(message)
                .setStyle(Style.EMPTY.withColor(color));
        this.imagetext.add(FzmmUtils.toNbtString(text, true));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public NbtList get() {
        return this.imagetext;
    }

    public Text getText() {
        MutableText text = Text.empty();

        List<Text> textList = this.getTextList();
        int size = textList.size();
        for (int i = 0; i != size; i++) {
            text.append(textList.get(i));
            if (i != size - 1)
                text.append("\n");
        }

        return text;
    }

    public List<Text> getTextList() {
        List<Text> textList = new ArrayList<>();

        for (var line : this.imagetext) {
            textList.add(Text.Serializer.fromJson(line.asString()));
        }

        return textList;
    }

    public boolean isEmpty() {
        return this.imagetext.isEmpty();
    }

    public String getCharacters() {
        return this.characters;
    }
}