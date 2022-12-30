package fzmm.zailer.me.client.logic.copyTextAlgorithm;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public class CopyTextAsJson extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "json";
    }


    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings) {
        Text text = Text.empty().setStyle(baseStyle);
        text.getSiblings().addAll(siblings);
        stringBuilder.append(Text.Serializer.toJson(text));
    }
}
