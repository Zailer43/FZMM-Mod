package fzmm.zailer.me.client.logic.copy_text_algorithm.algorithms;

import com.mojang.serialization.JsonOps;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.copy_text_algorithm.AbstractCopyTextAlgorithm;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;

import java.util.List;

public class CopyTextAsJson extends AbstractCopyTextAlgorithm {
    @Override
    public String getId() {
        return "json";
    }


    @Override
    protected void getStringRecursive(StringBuilder stringBuilder, Style baseStyle, List<Component> siblings) {
        Component text;
        if (siblings.size() == 1) {
            text = siblings.get(0);
        } else {
            text = Component.empty().setStyle(baseStyle);
            text.getSiblings().addAll(siblings);
        }

        assert Minecraft.getInstance().player != null;
        ComponentSerialization.CODEC.encodeStart(FzmmUtils.getRegistryOps(JsonOps.INSTANCE), text).result().ifPresentOrElse(stringBuilder::append, () ->
                FzmmClient.LOGGER.warn("[CopyTextAsJson] Failed to encode text to json")
        );
    }
}
