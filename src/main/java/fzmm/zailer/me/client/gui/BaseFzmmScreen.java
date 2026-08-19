package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.*;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.component.ETextureComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.extend.container.EScrollContainer;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import fzmm.zailer.me.client.gui.text_format.components.ColorListContainer;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.compat.symbol_chat.SymbolChatCompat;
import fzmm.zailer.me.compat.symbol_chat.components.FontTextBoxComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<EFlowLayout> implements ISnackBarScreen {
    @Nullable
    protected Screen parent;
    protected final String baseScreenTranslationKey;
    public static final int BUTTON_TEXT_PADDING = 8;
    public static final int COMPONENT_DISTANCE = 8;
    private final SymbolChatCompat symbolChatCompat;
    protected final FlowLayout snackBarLayout;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(EFlowLayout.class, DataSource.asset(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;
        this.symbolChatCompat = new SymbolChatCompat();
        this.snackBarLayout = new SnackBarLayout(Sizing.content(), Sizing.content());
    }

    @Override
    protected void build(EFlowLayout rootComponent) {
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null) {
            backButton.onPress(button -> this.onClose());
        }

        this.setup(rootComponent);
        rootComponent.child(this.snackBarLayout);
    }

    @Override
    protected void init() {
        super.init();
        Optional<EFlowLayout> root = this.getRoot();
        if (root.isEmpty()) return;

        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMemento memento) {
            FzmmHistory.restoreScreen(memento);
        }

        if (root.get().focusHandler() != null) {
            this.initFocus(root.get().focusHandler());
        }
    }

    protected void initFocus(FocusHandler focusHandler) {

    }

    protected abstract void setup(EFlowLayout rootComponent);

    @Override
    public void removed() {
        this.clearSnackBars();

        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMemento memento && !this.invalid) {
            FzmmHistory.saveScreen(memento);
        }

        super.removed();
    }

    @Override
    public void onClose() {
        this.setScreen(this.parent);
    }

    public String getBaseScreenTranslationKey() {
        return this.baseScreenTranslationKey;
    }

    public static String getBaseTranslationKey(Element element) {
        Screen currentScreen = Minecraft.getInstance().gui.screen();
        return currentScreen instanceof BaseFzmmScreen baseFzmmScreen ? baseFzmmScreen.getBaseScreenTranslationKey() : element.getAttribute("baseScreenTranslationKey");
    }

    public static String getBaseTranslationKey(String baseTranslationKey) {
        return "fzmm.gui." + baseTranslationKey;
    }

    public static String getOptionBaseTranslationKey(String baseScreenTranslationKey) {
        return getBaseTranslationKey(baseScreenTranslationKey) + ".option.";
    }

    public void child(UIComponent child) {
        this.uiAdapter.rootComponent.child(child);
    }

    @Override
    public FlowLayout getSnackBarLayout() {
        return this.snackBarLayout;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (this.symbolChatCompat.symbol().isMounted()) {
                this.symbolChatCompat.symbol().remove();
                this.symbolChatCompat.selectedComponent(null);
                return true;
            }

            if (this.symbolChatCompat.font().isMounted()) {
                this.symbolChatCompat.font().remove();
                this.symbolChatCompat.selectedComponent(null);
                return true;
            }
        }

        if (super.keyPressed(input)) return true;

        return this.symbolChatCompat.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (super.charTyped(input)) return true;

        return this.symbolChatCompat.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public SymbolChatCompat getSymbolChatCompat() {
        return this.symbolChatCompat;
    }

    static {
        // rows
        //TODO: replace rows with better UI components
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "button-row"), ButtonRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "color-row"), ColorRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "predicate-text-box-row"), ConfigTextBoxRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "context-menu-button-row"), ContextMenuButtonRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "image-rows"), ImageRows::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "number-row"), NumberRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "slider-row"), SliderRow::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "text-box-row"), TextBoxRow::parse);

        // extended components
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "boolean-button"), EBooleanButton::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "button"), element -> EComponents.button(net.minecraft.network.chat.Component.empty()));
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "item"), element -> EComponents.item(ItemStack.EMPTY));
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "label"), element -> EComponents.label(net.minecraft.network.chat.Component.empty()));
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "texture"), ETextureComponent::parse);

        // extended containers
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "flow-layout"), EFlowLayout::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "scroll"), EScrollContainer::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "tab-container"), TabContainer::parse);

        // these are necessary in case you want to create the fields manually with XML
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "book"), element -> new BookComponent());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "context-menu-button"), element -> new ContextMenuButton(net.minecraft.network.chat.Component.empty()));
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "number-slider"), element -> new SliderWidget());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "text-option"), element -> new ConfigTextBox());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "suggest-text-option"), element -> new SuggestionTextBox());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "image-option"), element -> new ImageButtonComponent());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "screenshot-zone"), element -> new ScreenshotZoneComponent());
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "color-list"), ColorListContainer::parse);
        UIParsing.registerFactory(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "font-text-box"), element -> new FontTextBoxComponent(Sizing.fixed(100)));

    }

    public UIModel getModel() {
        return this.model;
    }

    public Optional<EFlowLayout> getRoot() {
        if (this.uiAdapter == null) {
            return Optional.empty();
        }
        return Optional.of(this.uiAdapter.rootComponent);
    }
}
