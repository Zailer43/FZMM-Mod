package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.ScrollableButtonComponent;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.tabs.*;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.textformat.components.ColorListContainer;
import fzmm.zailer.me.client.gui.utils.memento.IMemento;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.compat.symbolChat.font.FontTextBoxComponent;
import fzmm.zailer.me.compat.symbolChat.SymbolChatCompat;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
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
    private final SymbolChatCompat symbolChatCompat;
    protected final HashMap<String, IScreenTab> tabs;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;
        this.tabs = new HashMap<>();
        this.symbolChatCompat = new SymbolChatCompat();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        assert this.client != null;
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null)
            backButton.onPress(button -> this.close());

        this.symbolChatCompat.addSymbolChatComponents(this);

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


    public void setTabs(Enum<? extends ITabsEnum> tabs) {
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

    public void child(Component child) {
        this.uiAdapter.rootComponent.child(child);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.symbolChatCompat.isSelectionPanelVisible()) {
                this.symbolChatCompat.setSelectionPanelVisible(false);
                return true;
            }

            if (this.symbolChatCompat.isFontSelectionVisible()) {
                this.symbolChatCompat.setFontSelectionVisible(false);
                return true;
            }
        }

        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return this.symbolChatCompat.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers))
            return true;

        return this.symbolChatCompat.charTyped(chr, modifiers);
    }

    public SymbolChatCompat getSymbolChatCompat() {
        return this.symbolChatCompat;
    }

    static {
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "boolean-row"), BooleanRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "button-row"), ButtonRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "color-row"), ColorRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "predicate-text-box-row"), ConfigTextBoxRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "enum-row"), EnumRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "image-rows"), ImageRows::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "number-row"), NumberRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "screen-tab-row"), ScreenTabRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "slider-row"), SliderRow::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "text-box-row"), TextBoxRow::parse);

        // these are necessary in case you want to create the fields manually with XML
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "boolean-button"), BooleanButton::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "number-slider"), element -> new SliderWidget());
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "text-option"), element -> new ConfigTextBox());
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "image-option"), element -> new ImageButtonComponent());
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "enum-option"), element -> new EnumWidget());
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "screen-tab"), ScreenTabContainer::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "screenshot-zone"), element -> new ScreenshotZoneComponent());
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "color-list"), ColorListContainer::parse);
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "font-text-box"), element -> new FontTextBoxComponent(Sizing.fixed(100)));
        UIParsing.registerFactory(new Identifier(FzmmClient.MOD_ID, "scrollable-text-button"), element -> new ScrollableButtonComponent(Text.empty(), buttonComponent -> {}));

    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    public static void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component id '%s'", componentTagName, id));
    }

    public UIModel getModel() {
        return this.model;
    }
}
