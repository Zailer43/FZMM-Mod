package fzmm.zailer.me.client.gui.headgenerator.options;

import fzmm.zailer.me.client.gui.components.IMode;

import java.awt.*;

public enum SkinPreEditOption implements IMode {
    NONE("none", (graphics, skin, skinPart) ->  {
        graphics.drawImage(skin,
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                null);

        graphics.drawImage(skin,
                skinPart.hatX(), skinPart.hatY(),
                skinPart.width() + skinPart.hatX(), skinPart.height() + skinPart.hatY(),
                skinPart.hatX(), skinPart.hatY(),
                skinPart.width() + skinPart.hatX(), skinPart.height() + skinPart.hatY(),
                null);
    }),
    OVERLAP("overlap", (graphics, skin, skinPart) -> {
        graphics.drawImage(skin,
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                null);

        graphics.drawImage(skin,
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                skinPart.hatX(), skinPart.hatY(),
                skinPart.width() + skinPart.hatX(), skinPart.height() + skinPart.hatY(),
                null);

        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(skinPart.hatX(), skinPart.hatY(), skinPart.width(), skinPart.height());
    }),
    REMOVE("remove", (graphics, skin, skinPart) -> {
        graphics.drawImage(skin,
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                skinPart.x(), skinPart.y(),
                skinPart.width() + skinPart.x(), skinPart.height() + skinPart.y(),
                null);

        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(skinPart.hatX(), skinPart.hatY(), skinPart.width(), skinPart.height());
    });

    private final String id;
    private final ISkinPreEdit preEdit;

    SkinPreEditOption(String id, ISkinPreEdit preEdit) {
        this.id = id;
        this.preEdit = preEdit;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.skinPreEdit." + this.id;
    }

    public ISkinPreEdit getPreEdit() {
        return this.preEdit;
    }
}