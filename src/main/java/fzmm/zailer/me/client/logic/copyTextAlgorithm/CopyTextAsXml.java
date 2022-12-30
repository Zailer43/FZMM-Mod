package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class CopyTextAsXml extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "xml";
    }

    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings) {
        for (var value : siblings) {
            stringBuilder.append(this.getColor(value.getStyle(), false))
                    .append(this.getBold(baseStyle, false))
                    .append(this.getItalic(baseStyle, false))
                    .append(this.getUnderline(baseStyle, false))
                    .append(this.getStrikethrough(baseStyle, false))
                    .append(this.getObfuscated(baseStyle, false))
                    .append(value.getString())
                    .append(this.getObfuscated(baseStyle, true))
                    .append(this.getStrikethrough(baseStyle, true))
                    .append(this.getUnderline(baseStyle, true))
                    .append(this.getItalic(baseStyle, true))
                    .append(this.getBold(baseStyle, true))
                    .append(this.getColor(value.getStyle(), true));

            if (!value.getSiblings().isEmpty())
                this.getStringRecursive(stringBuilder, baseStyle, value.getSiblings());
        }
    }

    public String getColor(Style style, boolean close) {
        if (style.getColor() == null)
            return "";
        return close ? ("<" + this.closeCharacter() + "color>") : ("<color:" + style.getColor().getHexCode() + ">");
    }

    public String closeCharacter() {
        return "/";
    }

    public String getBold(Style style, boolean close) {
        if (!style.isBold())
            return "";
        return "<" + (close ? this.closeCharacter() : "") + "bold>";
    }

    public String getItalic(Style style, boolean close) {
        if (!style.isItalic())
            return "";
        return "<" + (close ? this.closeCharacter() : "") + "italic>";
    }

    public String getUnderline(Style style, boolean close) {
        if (!style.isUnderlined())
            return "";
        return "<" + (close ? this.closeCharacter() : "") + "underline>";
    }

    public String getStrikethrough(Style style, boolean close) {
        if (!style.isStrikethrough())
            return "";
        return "<" + (close ? this.closeCharacter() : "") + "strikethrough>";
    }

    public String getObfuscated(Style style, boolean close) {
        if (!style.isObfuscated())
            return "";
        return "<" + (close ? this.closeCharacter() : "") + "obfuscated>";
    }
}
