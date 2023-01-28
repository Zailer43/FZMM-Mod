package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.gui.components.IMode;

public enum HeadGenerationMethod implements IMode {
    TEXTURE("texture"),
    MODEL("model");

    private final String name;

    HeadGenerationMethod(String name) {
        this.name = name;
    }
    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.generationMethod." + this.name;
    }

}