package fzmm.zailer.me.utils;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SnackBarLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class SnackBarManager {
    private static final Identifier SNACK_BAR_HUD = Identifier.of(FzmmClient.MOD_ID, "snack_bar");
    private static final int SNACK_BAR_LIMIT = 5;

    public static final String CLIPBOARD_ID = "clipboard";
    public static final String ENCRYPTOR_SAVE_ID = "encryptor_save";
    public static final String GIVE_ID = "give";
    public static final String HEAD_GALLERY_ID = "head_gallery";
    public static final String HEAD_GENERATOR_ID = "head_generator";
    public static final String HEAD_GENERATOR_SAVE_ID = "head_generator_save";
    public static final String IMAGE_ID = "load_image";
    public static final String IMAGETEXT_ID = "imagetext";
    public static final String INVISIBLE_ENTITY_ID = "invisible_entity";
    public static final String PLAYER_STATUE_ID = "player_statue";

    private static final SnackBarManager instance;
    private final ISnackBarScreen hudLayout;

    private SnackBarManager() {
        this.hudLayout = new SnackBarLayout(Sizing.content(), Sizing.content());
        this.initHud();
    }

    public static SnackBarManager getInstance() {
        return instance;
    }

    public SnackBarManager add(List<ISnackBarComponent> snackBarList) {
        ISnackBarScreen snackBarScreen = this.getSnackScreen();
        for (var snackBar : snackBarList) {
            this.add(snackBarScreen, snackBar);
        }
        return this;
    }

    public SnackBarManager add(ISnackBarComponent snackBar) {
        this.add(this.getSnackScreen(), snackBar);
        return this;
    }

    private void initHud() {
        if (!Hud.hasComponent(SNACK_BAR_HUD)) {
            Hud.add(SNACK_BAR_HUD, this.hudLayout::getSnackBarLayout);
        }
    }

    private void add(ISnackBarScreen snackBarScreen, ISnackBarComponent snackBar) {
        String id = snackBar.id();
        assert id != null;

        snackBarScreen.removeSnackBar(id);
        snackBarScreen.getSnackBarLayout().child(snackBar);

        if (snackBarScreen == this.hudLayout) {
            this.enableButtons(snackBarScreen, false);
        }
    }

    public SnackBarManager remove(String id) {
        for (var snackBar : this.getSnackScreen().getSnackBars()) {
            if (id.equals(snackBar.id())) {
                return this.remove(snackBar);
            }
        }
        return this;
    }

    public SnackBarManager remove(ISnackBarComponent snackBar) {
        this.getSnackScreen()
                .getSnackBarLayout()
                .removeChild(snackBar);
        return this;
    }

    public void removeOverflow() {
        ISnackBarScreen snackBarScreen = this.getSnackScreen();
        List<ISnackBarComponent> snackBarList = snackBarScreen.getSnackBars();

        if (snackBarList.size() > SNACK_BAR_LIMIT && snackBarList.stream().anyMatch(ISnackBarComponent::removeOnLimit)) {
            for (var snackBar : snackBarList) {
                if (snackBar.removeOnLimit()) {
                    snackBarScreen.removeSnackBar(snackBar);
                    break;
                }
            }
        }
    }

    private ISnackBarScreen getSnackScreen() {
        return MinecraftClient.getInstance().currentScreen instanceof ISnackBarScreen screen ? screen : this.hudLayout;
    }

    public void moveToHud(ISnackBarScreen from) {
        this.enableButtons(from, false);
        this.move(from, this.hudLayout);
    }

    public void moveToScreen(ISnackBarScreen to) {
        this.enableButtons(this.hudLayout, true);
        this.move(this.hudLayout, to);
    }

    public void move(ISnackBarScreen from, ISnackBarScreen to) {
        List<ISnackBarComponent> snackBarComponents = from.getSnackBars();
        from.clearSnackBars();
        for (var snackBar : snackBarComponents) {
            this.add(to, snackBar);
        }
    }

    private void enableButtons(ISnackBarScreen screen, boolean value) {
        for (var snackBar : screen.getSnackBars()) {
            snackBar.buttonsEnabled(value);
        }
    }

    public static void copyToClipboard(String text) {
        MinecraftClient.getInstance().keyboard.setClipboard(text);

        getInstance().add(BaseSnackBarComponent.builder(CLIPBOARD_ID)
                .backgroundColor(FzmmStyles.ALERT_SUCCESS_COLOR)
                .title(Text.translatable("fzmm.snack_bar.clipboard.title"))
                .lowTimer()
                .startTimer()
                .build()
        );
    }

    static {
        instance = new SnackBarManager();
    }
}
