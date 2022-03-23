package fzmm.zailer.me.client.guiLogic;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.ArrayList;

public class ImagetextLine {
    public static final String DEFAULT_TEXT = "█";
    private final ArrayList<Pair<Integer, Byte>> line;
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
            Pair<Integer, Byte> previous = this.line.get(size);
            byte previousAmount = previous.getRight();
            previous.setRight(++previousAmount);
            this.line.set(size, previous);
        } else {
            this.line.add(new Pair<>(color, (byte) 1));
        }

        return this;
    }

    public MutableText getLine(boolean disableItalic) {
        MutableText lineList = LiteralText.EMPTY.copy().setStyle(disableItalic ? Style.EMPTY.withItalic(false) : Style.EMPTY);
        short lineIndex = 0;
        for (int i = 0; i != this.line.size(); i++) {
            int color = this.line.get(i).getLeft();
            byte amount = this.line.get(i).getRight();
            Text line;

            if (this.isDefaultText && color == 0xFF000000) { // 255 0 0 0 (ARGB)
                String emptyString = " ".repeat(amount);
                line = new LiteralText(emptyString + Formatting.BOLD + emptyString + Formatting.RESET );
                lineIndex += amount;
            } else {
                StringBuilder textStrBuilder = new StringBuilder();
                Color pixel = new Color(color);
                color = (pixel.getRed() << 16) + (pixel.getGreen() << 8) + pixel.getBlue();

                for (int x = 0; x != amount; x++)
                    textStrBuilder.append(this.getCharacter(lineIndex++));

                line = new LiteralText(textStrBuilder.toString()).setStyle(Style.EMPTY.withColor(color));
            }
            lineList.append(line);
        }
        return lineList;
    }

    private String getCharacter(int index) {
        return this.text[index % this.text.length];
    }
}
