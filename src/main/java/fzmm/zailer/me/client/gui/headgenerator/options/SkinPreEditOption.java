package fzmm.zailer.me.client.gui.headgenerator.options;

import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.components.IMode;
import io.wispforest.owo.itemgroup.Icon;

import java.awt.*;

public enum SkinPreEditOption implements IMode {
    NONE("none", Icon.of(FzmmIcons.TEXTURE, 48, 0, 256, 256), (graphics, skin, skinPart) ->  {
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
    OVERLAP("overlap", Icon.of(FzmmIcons.TEXTURE, 48, 16, 256, 256), (graphics, skin, skinPart) -> {
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
    REMOVE("remove", Icon.of(FzmmIcons.TEXTURE, 48, 32, 256, 256), (graphics, skin, skinPart) -> {
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
    private final Icon icon;
    private final ISkinPreEdit preEdit;

    SkinPreEditOption(String id, Icon icon, ISkinPreEdit preEdit) {
        this.id = id;
        this.icon = icon;
        this.preEdit = preEdit;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.skinPreEdit." + this.id;
    }

    public ISkinPreEdit getPreEdit() {
        return this.preEdit;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String getId() {
        return "skin-pre-edit-" + this.id;
    }
}