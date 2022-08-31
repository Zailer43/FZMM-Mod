package fzmm.zailer.me.client.logic;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.ArrayList;

public class ImagetextLine {
    public static final String DEFAULT_TEXT = "█";
    private final ArrayList<Pair<Integer, Byte>> line; // color, number of pixels of the same color
    private final String[] text;
    private final boolean isDefaultText;

    public ImagetextLine(String text) {
        this.line = new ArrayList<>();
        this.text = text.split("");
        this.isDefaultText = text.equals(DEFAULT_TEXT);
    }

    public ImagetextLine add(int color) {
        int size = this.line.size();
        if (size > 0 && this.line.get(--size).getLeft() == color) {
            Pair<Integer, Byte> last = this.line.get(size);
            byte lastAmount = last.getRight();
            last.setRight(++lastAmount);
            this.line.set(size, last);
        } else {
            this.line.add(new Pair<>(color, (byte) 1));
        }

        return this;
    }

    public MutableText getLine() {
        MutableText lineList = Text.empty().setStyle(Style.EMPTY.withItalic(false));
        short lineIndex = 0;
        for (int i = 0; i != this.line.size(); i++) {
            int color = this.line.get(i).getLeft();
            int alpha = (color >> 24) & 0xFF;
            byte amount = this.line.get(i).getRight();
            Text line;

            if (this.isDefaultText && alpha == 0) {
                String spaceString = " ".repeat(amount);
                line = Text.literal(spaceString + Formatting.BOLD + spaceString + Formatting.RESET );
                lineIndex += amount;
            } else {
                StringBuilder textStrBuilder = new StringBuilder();
                Color pixel = new Color(color);
                color = (pixel.getRed() << 16) + (pixel.getGreen() << 8) + pixel.getBlue();

                for (int x = 0; x != amount; x++)
                    textStrBuilder.append(this.getCharacter(lineIndex++));

                line = Text.literal(textStrBuilder.toString()).setStyle(Style.EMPTY.withColor(color));
            }
            lineList.append(line);
        }
        return lineList;
    }

    private String getCharacter(int index) {
        return this.text[index % this.text.length];
    }
}
