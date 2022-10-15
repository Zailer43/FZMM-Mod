package fzmm.zailer.me.client.gui.widgets.image;

import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.toast.IStatus;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.text.Text;

public record ImageStatus(String detailsId, StatusType statusType) implements IStatus {
    private static final String BASE_TRANSLATION_KEY = "fzmm.toast.image.";
    public static final ImageStatus FILE_DOES_NOT_EXIST = new ImageStatus("fileDoesNotExist", StatusType.ERROR);
    public static final ImageStatus IMAGE_LOADED = new ImageStatus(null, StatusType.SUCCESSFUL);
    public static final ImageStatus INVALID_USERNAME = new ImageStatus("invalidUsername", StatusType.ERROR);
    public static final ImageStatus LOADING = new ImageStatus(null, StatusType.LOADING);
    public static final ImageStatus MALFORMED_URL = new ImageStatus("malformedUrl", StatusType.ERROR);
    public static final ImageStatus NO_IMAGE_LOADED = new ImageStatus("noImageLoaded", StatusType.ERROR);
    public static final ImageStatus PATH_DOES_NOT_HAVE_A_FILE = new ImageStatus("pathDoesNotHaveAFile", StatusType.ERROR);
    public static final ImageStatus UNEXPECTED_ERROR = new ImageStatus("unexpectedError", StatusType.ERROR);
    public static final ImageStatus URL_HAS_NO_IMAGE = new ImageStatus("urlHasNoImage", StatusType.ERROR);

    @Override
    public Text getStatusTranslation() {
        return this.statusType.getStatusTranslation();
    }

    @Override
    public Text getDetailsTranslation() {
        String detailsKey = ".details";
        if (this.detailsId != null)
            detailsKey += "." + this.detailsId;
        return Text.translatable(BASE_TRANSLATION_KEY + this.statusType.id + detailsKey);
    }

    @Override
    public Icon getIcon() {
        return this.statusType.getIcon();
    }

    @Override
    public int getOutlineColor() {
        return this.statusType.getColor();
    }

    @Override
    public int getBackgroundColor() {
        return 0x77000000;
    }

    public enum StatusType {
        LOADING("loading", FzmmIcons.LOADING, 0xA7FFFFFF),
        SUCCESSFUL("successful", FzmmIcons.SUCCESSFUL, 0x8700FF00),
        ERROR("error", FzmmIcons.ERROR, 0xA7FF0000);

        private final String id;
        private final Icon icon;
        private final int color;

        StatusType(String id, Icon icon, int color) {
            this.id = id;
            this.icon = icon;
            this.color = color;
        }

        public Text getStatusTranslation() {
            return Text.translatable(BASE_TRANSLATION_KEY + this.id + ".title");
        }

        public Icon getIcon() {
            return this.icon;
        }

        public int getColor() {
            return this.color;
        }
    }
}