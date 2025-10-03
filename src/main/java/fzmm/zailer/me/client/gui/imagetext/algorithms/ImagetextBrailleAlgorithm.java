package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;

public class ImagetextBrailleAlgorithm implements IImagetextAlgorithm {
    private static final String[] BRAILLE_CHARACTERS;
    private static final byte BRAILLE_CHARACTER_WIDTH = 2;
    private static final byte BRAILLE_CHARACTER_HEIGHT = 4;
    private SliderWidget edgeThresholdSlider;
    private SliderWidget edgeDistanceSlider;
    private SmallCheckboxComponent invertBooleanButton;
    private String[][] brailleImage = null;
    private BufferedImage colorsImage = null;
    private byte[][] grayScaleUpscaledImage = null;
    private final float widthRatio;

    public ImagetextBrailleAlgorithm() {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int defaultWidth = textRenderer.getWidth(ImagetextLine.DEFAULT_TEXT);
        int brailleWidth = textRenderer.getWidth(BRAILLE_CHARACTERS[BRAILLE_CHARACTERS.length - 1]);

        // Maybe they could become 0 with some resource pack?
        if (brailleWidth == 0 || defaultWidth == 0) {
            defaultWidth = 1;
            brailleWidth = 1;
        }
        this.widthRatio = brailleWidth / (float) defaultWidth;
    }

    @Override
    public BufferedImage image() {
        return this.colorsImage;
    }

    @Override
    public void image(BufferedImage image) {
        this.colorsImage = image;
    }

    @Override
    public void build() {
        if (this.grayScaleUpscaledImage == null) {
            FzmmClient.LOGGER.warn("[ImagetextBrailleAlgorithm] No image set");
            return;
        }
        this.brailleImage = this.brailleImageOf(
                this.grayScaleUpscaledImage,
                this.grayScaleUpscaledImage.length / BRAILLE_CHARACTER_WIDTH,
                this.grayScaleUpscaledImage[0].length / BRAILLE_CHARACTER_HEIGHT
        );
    }

    @Override
    public String getId() {
        return "algorithm.braille";
    }

    @Override
    public String[] linePixels(int line) {
        return this.brailleImage[line];
    }

    @Override
    public String pixelExample() {
        return BRAILLE_CHARACTERS[BRAILLE_CHARACTERS.length - 1]; // all characters are similar
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.edgeThresholdSlider = SliderRow.setup(rootComponent, "edgeThreshold", 30, 1, 255, Integer.class, 0, 5, null);
        this.edgeThresholdSlider.message(s -> {
            double percentage = this.edgeThresholdSlider.discreteValue() / 255f * 100;
            return Text.literal(new DecimalFormat("#,##0.0").format(percentage) + "%");
        });

        this.edgeDistanceSlider = SliderRow.setup(rootComponent, "edgeDistance", 2, 1, 5, Integer.class, 0, 1, null);
        this.invertBooleanButton = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "invert-checkbox");
        this.invertBooleanButton.checked(false);
    }

    @Override
    public float widthRatio() {
        return this.widthRatio;
    }

    @Override
    public float heightRatio() {
        return 1 / this.widthRatio;
    }

    @Override
    public void setUpdatePreviewCallback(Runnable callback) {
        this.edgeThresholdSlider.onChanged().subscribe(value -> callback.run());
        this.edgeDistanceSlider.onChanged().subscribe(value -> callback.run());
        this.invertBooleanButton.onChanged().subscribe(value -> callback.run());
    }

    @Override
    public boolean tryUpdateCache(ImagetextData data) {
        if (!IImagetextAlgorithm.super.tryUpdateCache(data)) return false;

        BufferedImage upscaledImage = ImageUtils.fastResizeImage(data.image(), data.width() * BRAILLE_CHARACTER_WIDTH,
                data.height() * BRAILLE_CHARACTER_HEIGHT, data.smoothRescaling()
        );
        this.grayScaleUpscaledImage = this.toGrayScale(upscaledImage);
        upscaledImage.flush();

        return true;
    }

    @Override
    public void clearCache() {
        IImagetextAlgorithm.super.clearCache();
        this.grayScaleUpscaledImage = null;
    }

    protected String[][] brailleImageOf(byte[][] grayScaleImage, int width, int height) {
        String[][] result = new String[height][width];
        int edgeThreshold = (int) this.edgeThresholdSlider.discreteValue();
        int edgeDistance = (int) this.edgeDistanceSlider.discreteValue();

        for (int y = 0; y != height; y++) {
            int yOffset = y * BRAILLE_CHARACTER_HEIGHT;
            for (int x = 0; x != width; x++) {
                int xOffset = x * BRAILLE_CHARACTER_WIDTH;
                result[y][x] = this.brailleCharOf(grayScaleImage, xOffset, yOffset, edgeThreshold, edgeDistance);
            }
        }

        return result;
    }

    /**
     * @param grayScaleImage the image must have width multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_WIDTH}
     *                       and height multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_HEIGHT}
     */
    protected String brailleCharOf(byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        int index = BRAILLE_CHARACTERS.length - 1;
        int yOffset = y;

        index -= this.brailleCharIndexOf(0, grayScaleImage, x, yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(1, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(2, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(3, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);

        yOffset = y;

        index -= this.brailleCharIndexOf(4, grayScaleImage, ++x, yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(5, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(6, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.brailleCharIndexOf(7, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);

        if (this.invertBooleanButton.checked()) {
            index = BRAILLE_CHARACTERS.length - 1 - index;
        }

        return BRAILLE_CHARACTERS[index];
    }

    protected int brailleCharIndexOf(int index, byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        return this.isEdge(grayScaleImage, x, y, edgeThreshold, edgeDistance) ? 1 << index : 0;
    }

    // pain
    protected boolean isEdge(byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        byte pixel = grayScaleImage[x][y];

        byte left = x < edgeDistance ? pixel : grayScaleImage[x - edgeDistance][y];
        byte right = x > grayScaleImage.length - edgeDistance - 1 ? pixel : grayScaleImage[x + edgeDistance][y];
        byte top = y < edgeDistance ? pixel : grayScaleImage[x][y - edgeDistance];
        byte bottom = y > grayScaleImage[0].length - edgeDistance - 1 ? pixel : grayScaleImage[x][y + edgeDistance];

        return this.isEdgeThreshold(pixel, left, edgeThreshold) ||
                this.isEdgeThreshold(pixel, right, edgeThreshold) ||
                this.isEdgeThreshold(pixel, top, edgeThreshold) ||
                this.isEdgeThreshold(pixel, bottom, edgeThreshold);
    }

    protected boolean isEdgeThreshold(byte pixelByte, byte edgePixelByte, int edgeThreshold) {
        int pixel = Byte.toUnsignedInt(pixelByte);
        int edgePixel = Byte.toUnsignedInt(edgePixelByte);
        return (pixel > edgePixel + edgeThreshold) || (pixel < edgePixel - edgeThreshold);
    }

    /**
     * The use of {@link BufferedImage#getRGB(int, int)} is avoided here because when the color is obtained with
     * {@link BufferedImage#getRGB(int, int)} the colors are combined, and since in braille the final imagetext
     * is upscaled, then there are more pixels and can be a bit more expensive
     */
    protected byte[][] toGrayScale(BufferedImage image) {
        byte[][] result = new byte[image.getWidth()][image.getHeight()];
        WritableRaster raster = image.getRaster();
        ColorModel colorModel = image.getColorModel();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Object elements = raster.getDataElements(x, y, null);
                int red = colorModel.getRed(elements);
                int green = colorModel.getGreen(elements);
                int blue = colorModel.getBlue(elements);
                int average = (red + green + blue) / 3;

                result[x][y] = (byte) average;
            }
        }

        return result;
    }

    @Override
    public IMementoObject createMemento() {
        return new BrailleAlgorithmMementoTab(
                (int) this.edgeThresholdSlider.discreteValue(),
                (int) this.edgeDistanceSlider.discreteValue(),
                this.invertBooleanButton.checked()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        BrailleAlgorithmMementoTab memento = (BrailleAlgorithmMementoTab) mementoObject;
        this.edgeThresholdSlider.setFromDiscreteValue(memento.edgeThreshold);
        this.edgeDistanceSlider.setFromDiscreteValue(memento.edgeDistance);
        this.invertBooleanButton.checked(memento.invert);
    }

    private record BrailleAlgorithmMementoTab(int edgeThreshold, int edgeDistance,
                                              boolean invert) implements IMementoObject {
    }

    static {
        BRAILLE_CHARACTERS = ("⠀⠁⠂⠃⠄⠅⠆⠇⡀⡁⡂⡃⡄⡅⡆⡇⠈⠉⠊⠋⠌⠍⠎⠏⡈⡉⡊⡋⡌⡍⡎⡏⠐⠑⠒⠓⠔⠕⠖⠗⡐⡑⡒⡓⡔⡕⡖⡗⠘⠙⠚⠛⠜⠝⠞⠟⡘⡙⡚⡛⡜⡝⡞⡟⠠⠡⠢⠣⠤⠥⠦⠧⡠⡡⡢⡣" +
                "⡤⡥⡦⡧⠨⠩⠪⠫⠬⠭⠮⠯⡨⡩⡪⡫⡬⡭⡮⡯⠰⠱⠲⠳⠴⠵⠶⠷⡰⡱⡲⡳⡴⡵⡶⡷⠸⠹⠺⠻⠼⠽⠾⠿⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⣀⣁⣂⣃⣄⣅⣆⣇⢈⢉⢊⢋⢌⢍⢎⢏⣈⣉⣊⣋⣌⣍⣎⣏⢐⢑⢒⢓⢔⢕" +
                "⢖⢗⣐⣑⣒⣓⣔⣕⣖⣗⢘⢙⢚⢛⢜⢝⢞⢟⣘⣙⣚⣛⣜⣝⣞⣟⢠⢡⢢⢣⢤⢥⢦⢧⣠⣡⣢⣣⣤⣥⣦⣧⢨⢩⢪⢫⢬⢭⢮⢯⣨⣩⣪⣫⣬⣭⣮⣯⢰⢱⢲⢳⢴⢵⢶⢷⣰⣱⣲⣳⣴⣵⣶⣷⢸⢹⢺⢻⢼⢽⢾⢿⣸⣹⣺⣻⣼⣽⣾⣿"
        ).split("");
    }
}
