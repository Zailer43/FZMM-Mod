package fzmm.zailer.me.client.logic.headGenerator;

import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public abstract class AbstractHeadEntry {

    private final Text displayName;
    private final String filterValue;
    private final String key;

    public AbstractHeadEntry(String displayName, String key) {
        this.displayName = Text.literal(displayName);
        this.key = key;
        this.filterValue = displayName.toLowerCase();
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public String getFilterValue() {
        return this.filterValue;
    }

    public String getKey() {
        return this.key;
    }

    public abstract BufferedImage getHeadSkin(BufferedImage baseSkin);

    public abstract String getCategoryId();

    public abstract boolean isEditingSkinBody();

    public abstract boolean isFirstResult();
}
