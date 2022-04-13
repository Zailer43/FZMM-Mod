package fzmm.zailer.me.client.gui.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.CustomConfigType;
import fzmm.zailer.me.client.gui.enums.FzmmIcons;
import fzmm.zailer.me.client.gui.enums.options.ImageModeOption;
import fzmm.zailer.me.client.gui.enums.options.SkinOption;
import fzmm.zailer.me.client.gui.interfaces.ICustomOption;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageOption extends ConfigBase<ImageOption> implements ICustomOption {

    private final String defaultValue;
    private String value;
    private String previousValue;
    private BufferedImage image;
    private ImageStatus status;
    public ConfigOptionList mode;

    public ImageOption(String name, String defaultValue, IConfigOptionListEntry defaultMode, String comment) {
        super(ConfigType.STRING, name, comment);

        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.previousValue = defaultValue;
        this.image = null;
        this.status = ImageStatus.NO_IMAGE_LOADED;
        this.mode = new ConfigOptionList("", defaultMode, "");
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive())
            return;
        JsonPrimitive jsonPrimitive = (JsonPrimitive) element;

        if (!jsonPrimitive.isString())
            return;

        this.setValueFromString(jsonPrimitive.getAsString());
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public boolean isModified() {
        return this.isModified(this.value);
    }

    @Override
    public boolean isModified(String newValue) {
        return !this.defaultValue.equals(newValue);
    }

    @Override
    public void resetToDefault() {
        this.setValueFromString(this.defaultValue);
    }

    @Override
    public String getDefaultStringValue() {
        return this.defaultValue;
    }

    @Override
    public void setValueFromString(String value) {
        this.previousValue = this.value;
        this.value = value;

        if (!this.previousValue.equals(this.value))
            this.onValueChanged();
    }

    @Override
    public String getStringValue() {
        return this.value;
    }

    @Override
    public CustomConfigType getConfigType() {
        return CustomConfigType.IMAGE;
    }

    @Nullable
    public BufferedImage getImage() {
        return this.image;
    }

    public boolean hasNoImage() {
        return this.image == null;
    }

    public void loadImage() {
        new Thread(() -> {
            IConfigOptionListEntry mode = this.getMode();
            this.status = ImageStatus.LOADING;

            if (ImageModeOption.URL == mode) {
                try {
                    this.image = FzmmUtils.getImageFromUrl(this.value);
                    if (this.image == null)
                        this.status = ImageStatus.URL_HAS_NO_IMAGE;
                    else
                        this.status = ImageStatus.IMAGE_LOADED;
                } catch (Exception e) {
                    this.status = ImageStatus.MALFORMED_URL;
                }
            } else if (ImageModeOption.PATH == mode || SkinOption.PATH == mode) {
                try {
                    if (!Files.exists(Paths.get(this.value))) {
                        this.status = ImageStatus.FILE_DOES_NOT_EXIST;
                        return;
                    }

                    File file = new File(this.value);
                    if (!file.isFile()) {
                        this.status = ImageStatus.PATH_DOES_NOT_HAVE_A_FILE;
                        return;
                    }

                    this.image = FzmmUtils.getImageFromPath(this.value);
                    this.status = ImageStatus.IMAGE_LOADED;
                } catch (Exception e) {
                    this.status = ImageStatus.UNEXPECTED_ERROR;
                }
            } else if (SkinOption.NAME == mode) {
                try {
                    if (!this.value.matches("^[a-zA-Z0-9_]{2,16}$")) {
                        this.status = ImageStatus.INVALID_USERNAME;
                        return;
                    }

                    this.image = FzmmUtils.getPlayerSkin(this.value);
                    if (this.image == null)
                        this.status = ImageStatus.INVALID_USERNAME;
                    else
                        this.status = ImageStatus.IMAGE_LOADED;
                } catch (Exception e) {
                    this.status = ImageStatus.UNEXPECTED_ERROR;
                }
            } else {
                this.status = ImageStatus.UNEXPECTED_ERROR;
            }
        }).start();
    }

    public IConfigOptionListEntry getMode() {
        return this.mode.getOptionListValue();
    }

    public int getModeWidth() {
        int width = 0;

        for (ImageModeOption imageMode : ImageModeOption.values())
            width = Math.max(width, imageMode.getWidth());

        return width + 10;
    }

    public String getStatusMessage() {
        return this.status.getMessage();
    }

    public FzmmIcons getStatusIcon() {
        return this.status.getIcon();
    }

    public record LoadImageListener(ImageOption option) implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            this.option.loadImage();
        }
    }

    public enum ImageStatus {
        FILE_DOES_NOT_EXIST("fileDoesNotExist", FzmmIcons.ERROR),
        IMAGE_LOADED("imageLoaded", FzmmIcons.SUCCESSFUL),
        INVALID_USERNAME("invalidUsername", FzmmIcons.ERROR),
        LOADING("loading", FzmmIcons.LOADING),
        MALFORMED_URL("malformedUrl", FzmmIcons.ERROR),
        NO_IMAGE_LOADED("noImageLoaded", FzmmIcons.ERROR),
        PATH_DOES_NOT_HAVE_A_FILE("pathDoesNotHaveAFile", FzmmIcons.ERROR),
        UNEXPECTED_ERROR("unexpectedError", FzmmIcons.ERROR),
        URL_HAS_NO_IMAGE("urlHasNoImage", FzmmIcons.ERROR);

        private static final String BASE_KEY = "fzmm.gui.option.image.";
        private final String message;
        private final FzmmIcons icon;

        ImageStatus(String message, FzmmIcons icon) {
            this.message = message;
            this.icon = icon;
        }

        public String getMessage() {
            return new TranslatableText(BASE_KEY + message).getString();
        }

        public FzmmIcons getIcon() {
            return this.icon;
        }
    }
}
