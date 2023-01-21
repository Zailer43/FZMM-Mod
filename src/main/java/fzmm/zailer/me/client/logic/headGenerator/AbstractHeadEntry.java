package fzmm.zailer.me.client.logic.headGenerator;

import fzmm.zailer.me.client.gui.headgenerator.HeadGenerationMethod;

import java.awt.image.BufferedImage;

public abstract class AbstractHeadEntry {

    private final String displayName;
    private final String key;

    public AbstractHeadEntry(String displayName, String key) {
        this.displayName = displayName;
        this.key = key;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getKey() {
        return this.key;
    }

    public abstract BufferedImage getHeadSkin(BufferedImage baseSkin, boolean overlapHatLayer);

    public abstract boolean canOverlap();

    public abstract HeadGenerationMethod getGenerationMethod();
}
