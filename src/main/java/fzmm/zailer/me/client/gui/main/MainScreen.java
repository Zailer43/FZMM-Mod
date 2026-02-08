package fzmm.zailer.me.client.gui.main;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.HistoryScreen;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.client.gui.encrypt_book.EncryptBookScreen;
import fzmm.zailer.me.client.gui.head_gallery.HeadGalleryScreen;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.imagetext.ImagetextScreen;
import fzmm.zailer.me.client.gui.player_statue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.text_format.TextFormatScreen;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.utils.HoverAnimationState;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MainScreen extends BaseFzmmScreen {
    private final HashMap<String, ButtonData> entries;
    private ELabelComponent hoveredLabel;

    public MainScreen(@Nullable Screen parent) {
        super("main", "main", parent);

        this.entries = this.getEntries();
    }

    private HashMap<String, ButtonData> getEntries() {
        HashMap<String, ButtonData> result = new LinkedHashMap<>();
        // maybe this should have a better order... and a config for it
        result.put("imagetext", new ButtonData(ImagetextScreen::new, FzmmIcons.IMAGETEXT));
        result.put("textFormat", new ButtonData(TextFormatScreen::new, FzmmIcons.TEXT_FORMAT));
        result.put("playerStatue", new ButtonData(PlayerStatueScreen::new, FzmmIcons.PLAYER_STATUE));
        result.put("encryptbook", new ButtonData(EncryptBookScreen::new, FzmmIcons.ENCRYPT_BOOK));
        result.put("headGenerator", new ButtonData(HeadGeneratorScreen::new, FzmmIcons.HEAD_GENERATOR));
        result.put("converters", new ButtonData(ConvertersScreen::new, FzmmIcons.CONVERTERS));
        result.put("history", new ButtonData(HistoryScreen::new, FzmmIcons.HISTORY, !FzmmHistory.getAllItems().isEmpty()));
        result.put("headGallery", new ButtonData(HeadGalleryScreen::new, FzmmIcons.HEAD_GALLERY));
        result.put("bannerEditor", new ButtonData(BannerEditorScreen::new, FzmmIcons.BANNER_EDITOR));

        return result;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setup(EFlowLayout rootComponent) {
        rootComponent.childByIdOrThrow(ButtonComponent.class, "config-button")
                .onPress(button -> this.minecraft.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this)));

        this.hoveredLabel = rootComponent.childByIdOrThrow(ELabelComponent.class, "hovered");
        List<UIComponent> entryLayoutList = new ArrayList<>();

        for (var id : this.entries.keySet()) {
            ButtonData entry = this.entries.get(id);
            StackLayout buttonLayout = this.getModel().expandTemplate(StackLayout.class, "main-button-layout", Map.of(
                    "name", id,
                    "u", String.valueOf(entry.u()),
                    "v", String.valueOf(entry.v())
            ));

            EButtonComponent button = buttonLayout.childById(EButtonComponent.class, id);
            assert button != null;

            button.active(entry.enabled());

            button.mouseEnter().subscribe(() -> this.selectEntry(button));
            button.focusGained().subscribe((focusSource) -> this.selectEntry(button));
            button.mouseLeave().subscribe(() -> this.unselectEntry(button));
            button.focusLost().subscribe(() -> this.unselectEntry(button));

            button.onPress(buttonComponent -> this.setScreen(entry.screenGetter().apply(this)));
            button.renderer(this.buttonRenderer());

            entryLayoutList.add(buttonLayout);
        }

        rootComponent.childByIdOrThrow(FlowLayout.class, "content").children(entryLayoutList);
    }

    private void selectEntry(EButtonComponent button) {
        this.hoveredLabel.text(this.buttonText(button.id()));
    }

    private void unselectEntry(EButtonComponent button) {
        if (this.hoveredLabel.text().equals(this.buttonText(button.id()))) {
            this.hoveredLabel.text(net.minecraft.network.chat.Component.empty());
        }
    }

    private net.minecraft.network.chat.Component buttonText(String id) {
        return net.minecraft.network.chat.Component.translatable("fzmm.gui.title." + id);
    }

    private ButtonComponent.Renderer buttonRenderer() {
        HoverAnimationState animationState = new HoverAnimationState(0.8f);

        // update button background color with animation if is hovered,
        // background alpha is between 50% and 100%, only have a background if the button is active
        // draw a border, white if is hovered
        return (context, button, delta) -> {
            boolean isHovered = button.isHovered() && button.active();
            double progress = animationState.update(isHovered);
            int alpha = Mth.clamp((int) (progress * 255), 128, 255);
            int borderColor = isHovered ? 0xFFFFFFFF : 0xFF466647;

            if (button.active()) {
                int background = 0x479E4A | (alpha << 24);
                context.fill(button.x(), button.y(), button.x() + button.width(), button.y() + button.height(), background);
            }
            context.drawRectOutline(button.x(), button.y(), button.width(), button.height(), borderColor);
        };
    }

    private record ButtonData(int u, int v, Function<MainScreen, Screen> screenGetter, boolean enabled) {

        public ButtonData(Function<MainScreen, Screen> screenGetter, int[] icon) {
            this(screenGetter, icon, true);
        }

        public ButtonData(Function<MainScreen, Screen> screenGetter, int[] icon, boolean enabled) {
            this(icon[0], icon[1], screenGetter, enabled);
        }
    }
}