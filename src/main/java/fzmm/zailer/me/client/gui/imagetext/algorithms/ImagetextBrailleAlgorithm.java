package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.MutableText;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextBrailleAlgorithm implements IImagetextAlgorithm {
    private static final String[] BRAILLE_CHARACTERS;
    private static final String EDGE_THRESHOLD_ID = "edgeThreshold";
    private static final String EDGE_DISTANCE_ID = "edgeDistance";
    private static final byte BRAILLE_CHARACTER_WIDTH = 2;
    private static final byte BRAILLE_CHARACTER_HEIGHT = 4;
    private SliderWidget edgeThresholdSlider;
    private SliderWidget edgeDistanceSlider;

    @Override
    public String getId() {
        return "algorithm.braille";
    }

    @Override
    public List<MutableText> get(ImagetextLogic logic, ImagetextData data, int lineSplitInterval) {
        BufferedImage resultSizeImage = logic.resizeImage(data.image(), data.width(), data.height(), data.smoothRescaling());
        BufferedImage upscaledImage = logic.resizeImage(data.image(), data.width() * BRAILLE_CHARACTER_WIDTH, data.height() * BRAILLE_CHARACTER_HEIGHT, data.smoothRescaling());
        BufferedImage grayScaleImage = this.toGrayScale(upscaledImage);
        List<String> charactersList = this.getBrailleCharacters(grayScaleImage, data.width(), data.height());
        List<MutableText> linesList = new ArrayList<>();

        for (int y = 0; y != data.height(); y++) {
            ImagetextLine line = new ImagetextLine(charactersList.get(y), data.percentageOfSimilarityToCompress(), lineSplitInterval);
            for (int x = 0; x != data.width(); x++) {
                line.add(resultSizeImage.getRGB(x, y));
            }

            linesList.addAll(line.getLineComponents());
        }

        return linesList;
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.edgeThresholdSlider = SliderRow.setup(rootComponent, EDGE_THRESHOLD_ID, 30, 1, 255, Integer.class, 0, 1, null);
        this.edgeDistanceSlider = SliderRow.setup(rootComponent, EDGE_DISTANCE_ID, 2, 1, 5, Integer.class, 0, 1, null);
    }

    @Override
    public String getCharacters() {
        return BRAILLE_CHARACTERS[BRAILLE_CHARACTERS.length - 1];
    }

    public List<String> getBrailleCharacters(BufferedImage grayScaleImage, int width, int height) {
        List<String> result = new ArrayList<>();
        int edgeThreshold = (int) this.edgeThresholdSlider.discreteValue();
        int edgeDistance = (int) this.edgeDistanceSlider.discreteValue();

        for (int y = 0; y != height; y++) {
            StringBuilder builder = new StringBuilder();
            int yOffset = y * BRAILLE_CHARACTER_HEIGHT;
            for (int x = 0; x != width; x++) {
                int xOffset = x * BRAILLE_CHARACTER_WIDTH;
                builder.append(this.getBrailleCharacter(grayScaleImage, xOffset, yOffset, edgeThreshold, edgeDistance));
            }
            result.add(builder.toString());
        }

        return result;
    }

    /**
     * @param grayScaleImage the image must have width multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_WIDTH}
     *                      and height multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_HEIGHT}
     */
    public String getBrailleCharacter(BufferedImage grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        int index = 0B11111111;
        int yOffset = y;

        if (this.isEdge(grayScaleImage, x, yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(0);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(1);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(2);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(3);

        yOffset = y;

        if (this.isEdge(grayScaleImage, ++x, yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(4);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(5);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(6);

        if (this.isEdge(grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance))
            index -= this.getBrailleCharacterIndex(7);

        return BRAILLE_CHARACTERS[index];
    }

    private int getBrailleCharacterIndex(int index) {
        return 1 << index;
    }

    // pain
    public boolean isEdge(BufferedImage grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        int pixel = grayScaleImage.getRGB(x, y);

        int left = x < edgeDistance ? pixel : grayScaleImage.getRGB(x - edgeDistance, y);
        int right = x > grayScaleImage.getWidth() - edgeDistance - 1 ? pixel : grayScaleImage.getRGB(x + edgeDistance, y);
        int top = y < edgeDistance ? pixel : grayScaleImage.getRGB(x, y - edgeDistance);
        int bottom = y > grayScaleImage.getHeight() - edgeDistance - 1 ? pixel : grayScaleImage.getRGB(x, y + edgeDistance);

        boolean isPixelLeftEdge = this.isEdgeThreshold(pixel, left, edgeThreshold);
        boolean isPixelRightEdge = this.isEdgeThreshold(pixel, right, edgeThreshold);
        boolean isPixelTopEdge = this.isEdgeThreshold(pixel, top, edgeThreshold);
        boolean isPixelBottomEdge = this.isEdgeThreshold(pixel, bottom, edgeThreshold);

        return isPixelLeftEdge || isPixelRightEdge || isPixelTopEdge || isPixelBottomEdge;
    }

    private boolean isEdgeThreshold(int pixel, int edgePixel, int edgeThreshold) {
        return (pixel > edgePixel + edgeThreshold) || (pixel < edgePixel - edgeThreshold);
    }

    /**
     * the image is in grayscale, but it saves the value of only in the blue channel,
     * so it would actually be bluescale, anyway I do this since they are all channels
     * with the same value, so it doesn't matter
     */
    public BufferedImage toGrayScale(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;
                int average = (red + green + blue) / 3;

                result.setRGB(x, y, average);
            }
        }

        return result;
    }

    @Override
    public IMementoObject createMemento() {
        return new BrailleAlgorithmMementoTab((int) this.edgeThresholdSlider.discreteValue(), (int) this.edgeDistanceSlider.discreteValue());
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        BrailleAlgorithmMementoTab memento = (BrailleAlgorithmMementoTab) mementoObject;
        this.edgeThresholdSlider.setFromDiscreteValue(memento.edgeThreshold);
        this.edgeDistanceSlider.setFromDiscreteValue(memento.edgeDistance);
    }

    private record BrailleAlgorithmMementoTab(int edgeThreshold, int edgeDistance) implements IMementoObject {
    }

    static {
        BRAILLE_CHARACTERS = "⠀⠁⠂⠃⠄⠅⠆⠇⡀⡁⡂⡃⡄⡅⡆⡇⠈⠉⠊⠋⠌⠍⠎⠏⡈⡉⡊⡋⡌⡍⡎⡏⠐⠑⠒⠓⠔⠕⠖⠗⡐⡑⡒⡓⡔⡕⡖⡗⠘⠙⠚⠛⠜⠝⠞⠟⡘⡙⡚⡛⡜⡝⡞⡟⠠⠡⠢⠣⠤⠥⠦⠧⡠⡡⡢⡣⡤⡥⡦⡧⠨⠩⠪⠫⠬⠭⠮⠯⡨⡩⡪⡫⡬⡭⡮⡯⠰⠱⠲⠳⠴⠵⠶⠷⡰⡱⡲⡳⡴⡵⡶⡷⠸⠹⠺⠻⠼⠽⠾⠿⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⣀⣁⣂⣃⣄⣅⣆⣇⢈⢉⢊⢋⢌⢍⢎⢏⣈⣉⣊⣋⣌⣍⣎⣏⢐⢑⢒⢓⢔⢕⢖⢗⣐⣑⣒⣓⣔⣕⣖⣗⢘⢙⢚⢛⢜⢝⢞⢟⣘⣙⣚⣛⣜⣝⣞⣟⢠⢡⢢⢣⢤⢥⢦⢧⣠⣡⣢⣣⣤⣥⣦⣧⢨⢩⢪⢫⢬⢭⢮⢯⣨⣩⣪⣫⣬⣭⣮⣯⢰⢱⢲⢳⢴⢵⢶⢷⣰⣱⣲⣳⣴⣵⣶⣷⢸⢹⢺⢻⢼⢽⢾⢿⣸⣹⣺⣻⣼⣽⣾⣿"
                .split("");
    }
}
