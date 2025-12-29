package fzmm.zailer.me.client.logic.head_generator;

import net.minecraft.network.chat.Component;

import java.awt.image.BufferedImage;
import java.util.Locale;

public abstract class AbstractHeadEntry {

    private final Component displayName;
    private final String filterValue;
    private final String path;

    public AbstractHeadEntry(String path) {
        String displayNameStr = this.toDisplayName(path);
        this.displayName = Component.literal(displayNameStr);
        this.path = path;
        this.filterValue = displayNameStr.toLowerCase(Locale.ROOT);
    }

    private String toDisplayName(String path) {
        if (path.isEmpty()) {
            return "";
        }

        String[] folders = path.split("/");
        String fileName = folders.length != 0 ? folders[folders.length - 1] : path;

        String displayName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
        displayName = displayName.replaceAll("_", " ");

        // If the displayName ends with a number and not have space before it, add a space before it
        if (displayName.matches(".*[0-9]+$") && !displayName.matches(".* [0-9]+$")){
            displayName = displayName.replaceFirst("([0-9]+$)", " $1");
        }

        return displayName;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public String getFilterValue() {
        return this.filterValue;
    }

    /**
     * @return path to the resource from HeadResourcesLoader.FZMM_MODELS_FOLDER
     * <p>
     * Example: "heads/plushie_1", "internal/plushie_base"
     */
    public String getPath() {
        return this.path;
    }

    public String getKey() {
        String[] split = this.path.split("/");
        return split[split.length - 1];
    }

    public abstract BufferedImage getHeadSkin(BufferedImage baseSkin, boolean hasUnusedPixels);

    public abstract String getCategoryId();

    public abstract boolean isEditingSkinBody();

    public abstract boolean isFirstResult();
}
