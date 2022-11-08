package fzmm.zailer.me.client.toast.status;

import fzmm.zailer.me.client.FzmmIcons;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.text.Text;

public class PlayerStatueStatus implements IStatus {
    private static final String BASE_TRANSLATION_KEY = "fzmm.toast.playerStatue.";
    public static final PlayerStatueStatus SUCCESSFUL = new PlayerStatueStatus("successful", StatusType.SUCCESSFUL);
    public static final PlayerStatueStatus LOADING = new PlayerStatueStatus("loading", StatusType.LOADING);
    public static final PlayerStatueStatus LOADING_SECOND_TRY = new PlayerStatueStatus("secondTry", StatusType.LOADING);
    public static final PlayerStatueStatus ERROR = new PlayerStatueStatus("error", StatusType.ERROR);
    private final String id;
    private final StatusType statusType;

    public PlayerStatueStatus(String id, StatusType statusType) {
        this.statusType = statusType;
        this.id = id;
    }

    @Override
    public Text getStatusTranslation() {
        return this.statusType.getStatusTranslation(this.id);
    }

    @Override
    public Text getDetailsTranslation(Object... args) {
        return Text.translatable(BASE_TRANSLATION_KEY + this.id + ".details", args);
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
        LOADING(FzmmIcons.LOADING, 0xA7FFFFFF),
        SUCCESSFUL(FzmmIcons.SUCCESSFUL, 0x8700FF00),
        ERROR(FzmmIcons.ERROR, 0xA7FF0000);

        private final Icon icon;
        private final int color;

        StatusType(Icon icon, int color) {
            this.icon = icon;
            this.color = color;
        }

        public Text getStatusTranslation(String id) {
            return Text.translatable(BASE_TRANSLATION_KEY + id + ".title");
        }

        public Icon getIcon() {
            return this.icon;
        }

        public int getColor() {
            return this.color;
        }
    }
}