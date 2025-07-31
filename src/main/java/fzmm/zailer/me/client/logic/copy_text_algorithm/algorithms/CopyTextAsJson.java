package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.copy_text_algorithm.AbstractCopyTextAlgorithm;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.List;

public class CopyTextAsJson extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "json";
    }


    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Text> siblings) {
        Text text;
        if (siblings.size() == 1) {
            text = siblings.get(0);
        } else {
            text = Text.empty().setStyle(baseStyle);
            text.getSiblings().addAll(siblings);
        }

        assert MinecraftClient.getInstance().player != null;
        TextCodecs.CODEC.encodeStart(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), text).result().ifPresentOrElse(stringBuilder::append, () ->
                FzmmClient.LOGGER.warn("[CopyTextAsJson] Failed to encode text to json")
        );
    }
}
