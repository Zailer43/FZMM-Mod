package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.ScreenTabContainer;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.main.components.MainButtonComponent;
import fzmm.zailer.me.client.gui.textformat.components.ColorListContainer;
import fzmm.zailer.me.compat.symbolChat.symbol.CustomSymbolSelectionPanel;
import fzmm.zailer.me.compat.symbolChat.symbol.SymbolSelectionPanelComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<FlowLayout> {
    @Nullable
    protected Screen parent;
    protected final String baseScreenTranslationKey;
    public static final int BUTTON_TEXT_PADDING = 8;
    public static final int COMPONENT_DISTANCE = 8;
    private final SymbolSelectionPanelComponent symbolSelectionPanel;
//    private final FontSelectionDropDownComponent fontSelectionDropDown;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;

        if (FzmmClient.SYMBOL_CHAT_PRESENT) {
                this.symbolSelectionPanel = new SymbolSelectionPanelComponent(CustomSymbolSelectionPanel.of(this, 0, 0));
//            this.fontSelectionDropDown = new FontSelectionDropDownComponent(new net.replaceitem.symbolchat.gui.widget.FontSelectionDropDownWidget(0,
//                    0,
//                    net.replaceitem.symbolchat.gui.SymbolSelectionPanel.WIDTH,
//                    15,
//                    net.replaceitem.symbolchat.FontProcessor.fontProcessors,
//                    net.replaceitem.symbolchat.SymbolChat.selectedFont
//            ));
        } else {
            this.symbolSelectionPanel = null;
//            this.fontSelectionDropDown = null;
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        assert this.client != null;
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null)
            backButton.onPress(button -> this.close());

        if (FzmmClient.SYMBOL_CHAT_PRESENT) {
            rootComponent.child(this.symbolSelectionPanel);
//            rootComponent.child(this.fontSelectionDropDown);
        }

        this.setupButtonsCallbacks(rootComponent);
    }

    protected abstract void setupButtonsCallbacks(FlowLayout rootComponent);

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    public void selectScreenTab(FlowLayout rootComponent, IScreenTab selectedTab) {
        for (var tab : selectedTab.getClass().getEnumConstants()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, ScreenTabContainer.getScreenTabId(tab));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, ScreenTabRow.getScreenTabButtonId(tab));

            if (screenTabContainer != null)
                screenTabContainer.setSelected(selectedTab == tab);

            if (screenTabButton != null)
                screenTabButton.active = tab != selectedTab;
        }
    }

    public String getBaseScreenTranslationKey() {
        return this.baseScreenTranslationKey;
    }

    public Optional<SymbolSelectionPanelComponent> getSymbolSelectionPanel() {
        return Optional.ofNullable(this.symbolSelectionPanel);
    }

//    public Optional<FontSelectionDropDownComponent> getFontSelectionDropDown() {
//        return Optional.ofNullable(this.fontSelectionDropDown);
//    }

    public static String getBaseTranslationKey(Element element) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        return currentScreen instanceof BaseFzmmScreen baseFzmmScreen ? baseFzmmScreen.getBaseScreenTranslationKey() : element.getAttribute("baseScreenTranslationKey");
    }

    public static String getBaseTranslationKey(String baseTranslationKey) {
        return "fzmm.gui." + baseTranslationKey;
    }

    public static String getTabTranslationKey(String baseScreenTranslationKey) {
        return getBaseTranslationKey(baseScreenTranslationKey) + ".tab.";
    }

    public static String getOptionBaseTranslationKey(String baseScreenTranslationKey) {
        return getBaseTranslationKey(baseScreenTranslationKey) + ".option.";
    }

    static {
        UIParsing.registerFactory("boolean-row", BooleanRow::parse);
        UIParsing.registerFactory("button-row", ButtonRow::parse);
        UIParsing.registerFactory("color-row", ColorRow::parse);
        UIParsing.registerFactory("predicate-text-box-row", ConfigTextBoxRow::parse);
        UIParsing.registerFactory("enum-row", EnumRow::parse);
        UIParsing.registerFactory("image-rows", ImageRows::parse);
        UIParsing.registerFactory("number-row", NumberRow::parse);
        UIParsing.registerFactory("screen-tab-row", ScreenTabRow::parse);
        UIParsing.registerFactory("slider-row", SliderRow::parse);
        UIParsing.registerFactory("text-box-row", TextBoxRow::parse);

        // these are necessary in case you want to create the fields manually with XML
        UIParsing.registerFactory("toggle-button", element -> new ConfigToggleButton());
        UIParsing.registerFactory("number-slider", element -> new SliderWidget());
        UIParsing.registerFactory("text-option", element -> new ConfigTextBox());
        UIParsing.registerFactory("image-option", element -> new ImageButtonComponent());
        UIParsing.registerFactory("enum-option", element -> new EnumWidget());
        UIParsing.registerFactory("screen-tab", ScreenTabContainer::parse);
        UIParsing.registerFactory("main-button", element -> new MainButtonComponent(Text.empty(), buttonComponent -> {}));
        UIParsing.registerFactory("screenshot-zone", element -> new ScreenshotZoneComponent());
        UIParsing.registerFactory("color-list", ColorListContainer::parse);

    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    public static void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component id '%s'", componentTagName, id));
    }
}
