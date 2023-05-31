package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.tabs.*;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.main.components.MainButtonComponent;
import fzmm.zailer.me.client.gui.textformat.components.ColorListContainer;
import fzmm.zailer.me.client.gui.utils.IMemento;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.gui.utils.IMementoScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.containers.VerticalGridLayout;
import fzmm.zailer.me.compat.CompatMods;
import fzmm.zailer.me.compat.symbolChat.symbol.CustomSymbolSelectionPanel;
import fzmm.zailer.me.compat.symbolChat.symbol.SymbolSelectionPanelComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<FlowLayout> {
    @Nullable
    protected Screen parent;
    protected final String baseScreenTranslationKey;
    public static final int BUTTON_TEXT_PADDING = 8;
    public static final int COMPONENT_DISTANCE = 8;
    private final CustomSymbolSelectionPanel customSymbolSelectionPanel;
    protected final HashMap<String, IScreenTab> tabs;
//    private final FontSelectionDropDownComponent fontSelectionDropDown;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;
        this.tabs = new HashMap<>();

        if (CompatMods.SYMBOL_CHAT_PRESENT) {
                this.customSymbolSelectionPanel = CustomSymbolSelectionPanel.of(0, 0);
//            this.fontSelectionDropDown = new FontSelectionDropDownComponent(new net.replaceitem.symbolchat.gui.widget.FontSelectionDropDownWidget(0,
//                    0,
//                    net.replaceitem.symbolchat.gui.SymbolSelectionPanel.WIDTH,
//                    15,
//                    net.replaceitem.symbolchat.FontProcessor.fontProcessors,
//                    net.replaceitem.symbolchat.SymbolChat.selectedFont
//            ));
        } else {
            this.customSymbolSelectionPanel = null;
//            this.fontSelectionDropDown = null;
        }
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        assert this.client != null;
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null)
            backButton.onPress(button -> this.close());

        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            rootComponent.child(new SymbolSelectionPanelComponent(this.customSymbolSelectionPanel.parent()));
//            rootComponent.child(this.fontSelectionDropDown);
        }

        this.setupButtonsCallbacks(rootComponent);

        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMementoScreen mementoScreen)
            mementoScreen.getMemento().ifPresent(mementoScreen::restoreMemento);
    }

    protected abstract void setupButtonsCallbacks(FlowLayout rootComponent);

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);

        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMementoScreen mementoScreen) {
            try {
                mementoScreen.setMemento(mementoScreen.createMemento());
            } catch (NullPointerException e) {
                FzmmClient.LOGGER.error("Failed to create memento", e);
            }
        }
    }


    protected void setTabs(Enum<? extends ITabsEnum> tabs) {
        this.setTabs(this.tabs, tabs);
    }

    protected void setTabs(HashMap<String, IScreenTab> hashMap, Enum<? extends ITabsEnum> tabs) {
        for (var tab : tabs.getDeclaringClass().getEnumConstants())
            hashMap.put(tab.getId(), tab.createTab());
    }

    protected HashMap<String, IMementoObject> createMementoTabs() {
        return this.createMementoTabs(this.tabs);
    }

    protected HashMap<String, IMementoObject> createMementoTabs(HashMap<String, IScreenTab> tabsHashMap) {
        HashMap<String, IMementoObject> tabs = new HashMap<>();
        for (var tab : tabsHashMap.values()) {
            if (tab instanceof IMemento mementoTab)
                tabs.put(tab.getId(), mementoTab.createMemento());

        }
        return tabs;
    }

    protected void restoreMementoTabs(HashMap<String, IMementoObject> mementoTabs) {
        this.restoreMementoTabs(mementoTabs, this.tabs);
    }

    protected void restoreMementoTabs(HashMap<String, IMementoObject> mementoTabs, HashMap<String, IScreenTab> tabsHashMap) {
        for (var tab : tabsHashMap.values()) {
            if (tab instanceof IMemento mementoTab)
                mementoTab.restoreMemento(mementoTabs.get(tab.getId()));
        }
    }

    public <T extends Enum<? extends IScreenTabIdentifier>> T selectScreenTab(FlowLayout rootComponent, IScreenTabIdentifier selectedTab, T tabs) {
        return this.selectScreenTab(rootComponent, selectedTab, tabs, this.tabs);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<? extends IScreenTabIdentifier>> T selectScreenTab(FlowLayout rootComponent, IScreenTabIdentifier selectedTab, T tabs, HashMap<String, IScreenTab> tabsHashMap) {
        for (var tabId : tabsHashMap.keySet()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, ScreenTabContainer.getScreenTabId(tabId));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, ScreenTabRow.getScreenTabButtonId(tabId));
            boolean isSelectedTab = selectedTab.getId().equals(tabId);
            if (screenTabContainer != null)
                screenTabContainer.setSelected(isSelectedTab);

            if (screenTabButton != null)
                screenTabButton.active = !isSelectedTab;
        }


        Optional<T> result = (Optional<T>) Arrays.stream(tabs.getDeclaringClass().getEnumConstants())
                .filter(tab -> tab.getId().equals(selectedTab.getId()))
                .findFirst();

        assert result.isPresent();

        return result.get();
    }

    public <T extends IScreenTab> T getTab(IScreenTabIdentifier tab, Class<T> ignored) {
        return this.getTab(tab, ignored, this.tabs);
    }

    @SuppressWarnings("unchecked")
    public <T extends IScreenTab> T getTab(IScreenTabIdentifier tab, Class<T> ignored, HashMap<String, IScreenTab> tabsHashMap) {
        return (T) tabsHashMap.get(tab.getId());
    }

    public String getBaseScreenTranslationKey() {
        return this.baseScreenTranslationKey;
    }

    public Optional<CustomSymbolSelectionPanel> getCustomSymbolSelectionPanel() {
        return Optional.ofNullable(this.customSymbolSelectionPanel);
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
        UIParsing.registerFactory("boolean-button", BooleanButton::parse);
        UIParsing.registerFactory("number-slider", element -> new SliderWidget());
        UIParsing.registerFactory("text-option", element -> new ConfigTextBox());
        UIParsing.registerFactory("image-option", element -> new ImageButtonComponent());
        UIParsing.registerFactory("enum-option", element -> new EnumWidget());
        UIParsing.registerFactory("screen-tab", ScreenTabContainer::parse);
        UIParsing.registerFactory("main-button", element -> new MainButtonComponent(Text.empty(), buttonComponent -> {}));
        UIParsing.registerFactory("screenshot-zone", element -> new ScreenshotZoneComponent());
        UIParsing.registerFactory("color-list", ColorListContainer::parse);
        UIParsing.registerFactory("vertical-grid-layout", VerticalGridLayout::parse);

    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    public static void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component id '%s'", componentTagName, id));
    }

    public UIModel getModel() {
        return this.model;
    }
}
