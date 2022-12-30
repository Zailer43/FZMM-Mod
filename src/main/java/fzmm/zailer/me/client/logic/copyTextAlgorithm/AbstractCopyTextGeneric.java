package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class AbstractCopyTextGeneric extends AbstractCopyTextAlgorithm {

    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings) {
        for (var value : siblings) {
            stringBuilder.append(this.getColor(value.getStyle()))
                    .append(this.getBold(baseStyle))
                    .append(this.getItalic(baseStyle))
                    .append(this.getUnderline(baseStyle))
                    .append(this.getStrikethrough(baseStyle))
                    .append(this.getObfuscated(baseStyle))
                    .append(value.getString());

            if (!value.getSiblings().isEmpty())
                this.getStringRecursive(stringBuilder, baseStyle, value.getSiblings());
        }
    }

    public abstract String getColor(Style style);

    public String colorCharacter() {
        return "&";
    }

    public String getBold(Style style) {
        return style.isBold() ? this.colorCharacter() + Formatting.BOLD.getCode() : "";
    }

    public String getItalic(Style style) {
        return style.isItalic() ? this.colorCharacter() + Formatting.ITALIC.getCode() : "";
    }

    public String getUnderline(Style style) {
        return style.isUnderlined() ? this.colorCharacter() + Formatting.UNDERLINE.getCode() : "";
    }

    public String getStrikethrough(Style style) {
        return style.isStrikethrough() ? this.colorCharacter() + Formatting.STRIKETHROUGH.getCode() : "";
    }

    public String getObfuscated(Style style) {
        return style.isObfuscated() ? this.colorCharacter() + Formatting.OBFUSCATED.getCode() : "";
    }
}
