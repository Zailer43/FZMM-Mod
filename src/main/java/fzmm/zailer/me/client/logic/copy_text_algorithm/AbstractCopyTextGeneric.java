package fzmm.zailer.me.client.logic.copy_text_algorithm;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public abstract class AbstractCopyTextGeneric extends AbstractCopyTextAlgorithm {

    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Component> siblings) {
        for (var value : siblings) {
            stringBuilder.append(this.getColor(value.getStyle()))
                    .append(this.getBold(baseStyle))
                    .append(this.getItalic(baseStyle))
                    .append(this.getUnderline(baseStyle))
                    .append(this.getStrikethrough(baseStyle))
                    .append(this.getObfuscated(baseStyle))
                    .append(value.plainCopy().getString());

            if (!value.getSiblings().isEmpty())
                this.getStringRecursive(stringBuilder, baseStyle, value.getSiblings());
        }
    }

    public abstract String getColor(Style style);

    public String colorCharacter() {
        return "&";
    }

    public String getBold(Style style) {
        return style.isBold() ? this.colorCharacter() + ChatFormatting.BOLD.getChar() : "";
    }

    public String getItalic(Style style) {
        return style.isItalic() ? this.colorCharacter() + ChatFormatting.ITALIC.getChar() : "";
    }

    public String getUnderline(Style style) {
        return style.isUnderlined() ? this.colorCharacter() + ChatFormatting.UNDERLINE.getChar() : "";
    }

    public String getStrikethrough(Style style) {
        return style.isStrikethrough() ? this.colorCharacter() + ChatFormatting.STRIKETHROUGH.getChar() : "";
    }

    public String getObfuscated(Style style) {
        return style.isObfuscated() ? this.colorCharacter() + ChatFormatting.OBFUSCATED.getChar() : "";
    }
}
