package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.text.Text;

public enum HeadGenerationMethod implements IMode {
    TEXTURE("texture"),
    MODEL("model");

    private final String name;

    HeadGenerationMethod(String name) {
        this.name = name;
    }
    @Override
    public Text getTranslation() {
        return Text.translatable("fzmm.gui.headGenerator.option.generationMethod." + this.name);
    }

}