package fzmm.zailer.me.client.gui.enums;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fzmm.zailer.me.client.gui.ScreenConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;

public enum Buttons {
    BACK("button.back"),
    CONVERTERS_COPY_ARRAY("converters.copyArray"),
    CONVERTERS_COPY_UUID("converters.copyUuid"),
    CONVERTERS_COPY_DECODED("converters.copyDecoded"),
    CONVERTERS_COPY_ENCODED("converters.copyEncoded"),
    ENCRYPTBOOK_GET_DECODER("encryptbook.getDecoder"),
    EXECUTE("button.execute"),
    FAQ("button.faq"),
    GIVE("button.give"),
    GRADIENT_ADD_LORE("gradient.addLore"),
    GRADIENT_COPY("gradient.copy"),
    GRADIENT_RANDOM_COLOR("gradient.randomColors"),
    GRADIENT_SET_NAME("gradient.setName"),
    LOAD_IMAGE("button.loadImage"),
    MAIN_CONFIGURATION("main.configs"),
    MAIN_CONVERTERS("main.converters"),
    MAIN_ENCRYPTBOOK("main.encryptbook"),
    MAIN_GRADIENT("main.gradient"),
    MAIN_IMAGETEXT("main.imagetext"),
    MAIN_ITEMS_EDITOR("main.itemEditor"),
    MAIN_PLAYER_STATUE("main.playerStatue"),
    PLAYER_STATUE_LAST_GENERATED("playerStatue.lastGenerated"),
    RANDOM("button.random");

    private final String key;
    @Nullable
    private final FzmmIcons icon;
    static final String INITIAL_TRANSLATION_KEY = "fzmm.gui.";

    Buttons(String key, @Nullable FzmmIcons icon) {
        this.key = key;
        this.icon = icon;
    }

    Buttons(String key) {
        this(key, null);
    }

    public String getText() {
        return new TranslatableText(INITIAL_TRANSLATION_KEY + this.key).getString();
    }

    @Nullable
    public FzmmIcons getIcon() {
        return this.icon;
    }

    public ButtonGeneric getToLeft(int x, int y) {
        int width = MinecraftClient.getInstance().textRenderer.getWidth(this.getText());
        return this.get(x - width, y, -1, ScreenConstants.NORMAL_BUTTON_HEIGHT);
    }

    public ButtonGeneric get(int x, int y) {
        return this.get(x, y, -1, ScreenConstants.NORMAL_BUTTON_HEIGHT);
    }

    public ButtonGeneric get(int x, int y, int width) {
        return this.get(x, y, width, ScreenConstants.NORMAL_BUTTON_HEIGHT);
    }

    public ButtonGeneric get(int x, int y, int width, int height) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, height, this.getText(), this.getIcon());
        button.setTextCentered(true);
        return button;
    }
}