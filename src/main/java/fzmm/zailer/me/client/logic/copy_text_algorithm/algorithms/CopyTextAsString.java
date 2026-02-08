package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

import fzmm.zailer.me.client.logic.copy_text_algorithm.AbstractCopyTextAlgorithm;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public class CopyTextAsString extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "string";
    }

    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Component> siblings) {
        for (var value : siblings)
            stringBuilder.append(value.getString());
    }

}
