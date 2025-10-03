package fzmm.zailer.me.client.logic.imagetext;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class ImagetextLine {
    public static final String DEFAULT_TEXT = "█";
    private static final long LINE_WRAPPER_LENGTH;
    private static final long ELEMENT_LENGTH;
    private boolean isDefaultText;
    private final double similarityThreshold;
    private String[] characters;
    private ImagetextLineElement element;
    private MutableText line;
    private int elementIndex;
    private long textLength;

    public ImagetextLine(double similarityThreshold) {
        this.characters(new String[]{DEFAULT_TEXT});
        this.similarityThreshold = similarityThreshold;
        this.reset();
    }

    public void reset() {
        this.element = null;
        this.line = Text.empty().setStyle(Style.EMPTY.withItalic(false));
        this.elementIndex = 0;
        this.textLength = 0L;
    }

    public void characters(String[] value) {
        this.characters = value;
        this.isDefaultText = value.length == 1 && value[0].equals(DEFAULT_TEXT);
    }

    public ImagetextLine add(int color) {
        if (this.element == null) { // first
            this.element = new ImagetextLineElement(color, this.isDefaultText);
        } else if (this.element.isSimilar(color, this.similarityThreshold)) {
            // [blue] -> [blue blue]
            this.element.increment();
        } else {
            // [red red red] -> [red red red] [green]
            this.nextComponent(color);
        }

        return this;
    }

    private void nextComponent(int color) {
        if (this.element == null) return;

        Text elementText = this.element.toText(this.characters, this.elementIndex);
        this.incrementTextLength(elementText.getString());
        this.line.append(elementText);
        this.elementIndex += this.element.getRepetitions();

        this.element.reset(color, this.isDefaultText);
    }

    public Text build() {
        this.nextComponent(-1);
        return this.line;
    }

    private void incrementTextLength(String characters) {
        // Estimate the length to display that information, this is preferred over converting
        // Text to JSON (for example using Codecs to JSON and then JsonElement::toString),
        // because that conversion is usually more expensive, therefore, calling that every
        // time the Imagetext preview needs to be updated is not ideal
        //
        // besides, it doesn't have to be that precise... right?
        // I think it may be necessary because some anticheats get angry if your item exceeds a certain size,
        // but in my experience, it wasn't very accurate before either in keeping the item within the allowed limit
        this.textLength += characters.length() + ELEMENT_LENGTH + 1; // 1 for the comma
    }

    public long textLength() {
        return this.textLength + LINE_WRAPPER_LENGTH + 1; // 1 for line wrapper comma
    }

    private static long textLength(Text text) {
        return TextCodecs.CODEC.encodeStart(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), text)
                .result()
                .map(JsonElement::toString)
                .orElse(DEFAULT_TEXT)
                .length() - 1;
    }

    static {
        Text defaultText = Text.literal(DEFAULT_TEXT).setStyle(Style.EMPTY.withColor(0x123456));
        ELEMENT_LENGTH = textLength(defaultText) - 1L; // -1 for DEFAULT_TEXT
        LINE_WRAPPER_LENGTH = textLength(Text.empty().setStyle(Style.EMPTY.withItalic(false)).append(defaultText)) - ELEMENT_LENGTH;
    }
}
