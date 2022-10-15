package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.widgets.EnumWidget;
import fzmm.zailer.me.client.gui.widgets.IMode;
import fzmm.zailer.me.client.gui.widgets.ScreenTabContainer;
import fzmm.zailer.me.client.gui.widgets.SliderWidget;
import fzmm.zailer.me.client.gui.widgets.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.widgets.image.mode.IImageMode;
import fzmm.zailer.me.client.gui.widgets.image.source.IImageSource;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class BaseFzmmScreen extends BaseUIModelScreen<FlowLayout> {
    private static final int COMPONENT_DISTANCE = 8;
    @Nullable
    protected Screen parent;
    protected final String baseTranslationKey;

    public BaseFzmmScreen(String screenPath, String baseTranslationKey, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(new Identifier(FzmmClient.MOD_ID, screenPath)));
        this.baseTranslationKey = baseTranslationKey;
        this.parent = parent;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        assert this.client != null;
        ButtonWidget backButton = rootComponent.childById(ButtonWidget.class, "back-button");
        if (backButton != null)
            backButton.onPress(button -> this.client.setScreen(this.parent));

        this.tryAddComponentList(rootComponent);
        this.setupButtonsCallbacks(rootComponent);
    }

    protected void tryAddComponentList(FlowLayout rootComponent) {
    };

    protected abstract void setupButtonsCallbacks(FlowLayout rootComponent);

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setupEnum(FlowLayout rootComponent, String id, Enum<? extends IMode> defaultValue, @Nullable ButtonWidget.PressAction callback) {
        EnumWidget enumButton = rootComponent.childById(EnumWidget.class, this.getEnumId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(enumButton, "enum-option", this.getEnumId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        enumButton.init(defaultValue);
        enumButton.onPress(button -> {
            if (callback != null)
                callback.onPress(button);
            resetButton.active = enumButton.parsedValue() != defaultValue;
        });

        resetButton.onPress(button -> {
            enumButton.select(defaultValue.ordinal());
            resetButton.active = false;
        });
        resetButton.onPress();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setupImageButton(FlowLayout rootComponent, String id, IImageSource defaultMode) {
        ImageButtonWidget imageButtonWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(id));
        ConfigTextBox imageValueField = rootComponent.childById(ConfigTextBox.class, this.getImageValueFieldId(id));

        this.checkNull(imageButtonWidget, "image-option", this.getImageButtonId(id));
        this.checkNull(imageValueField, "text-option", this.getImageValueFieldId(id));

        imageValueField.applyPredicate(defaultMode::predicate);
        imageButtonWidget.onPress(button -> imageButtonWidget.loadImage(imageValueField.getText()));
        imageButtonWidget.setSourceType(defaultMode);
        assert this.client != null;
        imageButtonWidget.horizontalSizing(Sizing.fixed(this.client.textRenderer.getWidth(imageButtonWidget.getMessage()) + 8));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setupSlider(FlowLayout rootComponent, String id, double defaultValue, double min,
                            double max, Class<? extends Number> numberType, @Nullable Consumer<Double> callback) {
        SliderWidget numberSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(numberSlider, "number-slider", this.getSliderId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        numberSlider.valueType(numberType);
        numberSlider.onChanged(aDouble -> {
            resetButton.active = numberSlider.value() != defaultValue;
            if (callback != null)
                callback.accept(aDouble);
        });
        numberSlider.min(min);
        numberSlider.max(max);
        numberSlider.setFromDiscreteValue(defaultValue);

        resetButton.onPress(button -> numberSlider.setFromDiscreteValue(defaultValue));
    }

    public void setupTextField(FlowLayout rootComponent, String id, String defaultValue) {
        TextFieldWidget textField = rootComponent.childById(TextFieldWidget.class, this.getTextFieldId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(textField, "text-box", this.getTextFieldId(id));

        textField.setChangedListener(button -> {
            if (resetButton != null)
                resetButton.active = !textField.getText().equals(defaultValue);
        });
        textField.setText(defaultValue);
        textField.setCursor(0);

        if (resetButton != null)
            resetButton.onPress(button -> textField.setText(defaultValue));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setupNumberField(FlowLayout rootComponent, String id, String defaultValue) {
        ConfigTextBox numberField = rootComponent.childById(ConfigTextBox.class, this.getNumberFieldId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(numberField, "text-option", this.getNumberFieldId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        numberField.setChangedListener(button -> resetButton.active = !numberField.getText().equals(defaultValue));
        numberField.setText(defaultValue);
        numberField.setCursor(0);

        resetButton.onPress(button -> numberField.setText(defaultValue));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void setupBooleanButton(FlowLayout rootComponent, String id, boolean defaultValue) {
        ConfigToggleButton toggleButton = rootComponent.childById(ConfigToggleButton.class, this.getToggleButtonId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(toggleButton, "toggle-button", this.getToggleButtonId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        toggleButton.enabled(defaultValue);
        toggleButton.onPress(button -> resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue);
        assert this.client != null;
        int maxWidth = Math.max(
                this.client.textRenderer.getWidth(Text.translatable("text.owo.config.boolean_toggle.enabled")),
                this.client.textRenderer.getWidth(Text.translatable("text.owo.config.boolean_toggle.disabled"))
        );
        toggleButton.horizontalSizing(Sizing.fixed(maxWidth + 8));

        resetButton.onPress(button -> toggleButton.enabled(defaultValue));
        resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue;
    }

    public void setupButton(FlowLayout rootComponent, String id, boolean enabled, ButtonWidget.PressAction callback) {
        ButtonWidget button = rootComponent.childById(ButtonWidget.class, id);

        this.checkNull(button, "button", id);

        button.active = enabled;
        button.onPress(callback);
    }

    @SuppressWarnings("ConstantConditions")
    public void setupImage(FlowLayout rootComponent, String imageButtonId, String imageEnumId, Enum<? extends IImageMode> defaultValue) {
        this.setupImageButton(rootComponent, imageButtonId, ((IImageMode) defaultValue).getSourceType());
        ImageButtonWidget imageWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(imageButtonId));
        this.setupEnum(rootComponent, imageEnumId, defaultValue, button -> {
            IImageMode mode = (IImageMode) ((EnumWidget) button).getValue();
            IImageSource sourceType = mode.getSourceType();
            imageWidget.setSourceType(sourceType);
            //noinspection UnstableApiUsage
            rootComponent.childById(ConfigTextBox.class, this.getImageValueFieldId(imageButtonId))
                    .applyPredicate(sourceType::predicate);
        });
    }

    public void tryAddComponentList(FlowLayout rootComponent, String containerId, Component... components) {
        FlowLayout container = rootComponent.childById(FlowLayout.class, containerId);
        if (container != null)
            container.children(new ArrayList<>(Arrays.stream(components).toList()));
    }

    public Component getLabel(String id, String tooltipId) {
        return Components
                .label(Text.translatable(this.getBaseTranslationKey() + ".option." + id))
                .tooltip(Text.translatable(this.getBaseTranslationKey() + ".option." + tooltipId + ".tooltip"))
                .margins(Insets.left(20))
                .id(this.getLabelId(id));
    }

    public Component getResetButton(String id) {
        return Components
                .button(Text.translatable("fzmm.gui.button.reset"), (Consumer<ButtonComponent>) buttonComponent -> {})
                .id(this.getResetId(id))
                .margins(Insets.right(20).withLeft(COMPONENT_DISTANCE));
    }

    public Component getTextFieldRow(String id) {
        return this.getTextFieldRow(id, false);
    }

    public Component getTextFieldRow(String id, boolean hasResetButton) {
        Component textField = Components
                .textBox(Sizing.fixed(200))
                .id(this.getTextFieldId(id));
        return this.getRow(id, id, hasResetButton, textField);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Component getNumberRow(String id, Class<? extends Number> numberType) {
        Component textField = new ConfigTextBox()
                .configureForNumber(numberType)
                .horizontalSizing(Sizing.fixed(200))
                .id(this.getNumberFieldId(id));

        return this.getRow(id, id, true, textField);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Component getBooleanRow(String id) {
        Component textField = new ConfigToggleButton()
                .id(this.getToggleButtonId(id));
        return this.getRow(id, id, true, textField);
    }

    public Component getSliderRow(String id, String tooltipId) {
        Component slider = new SliderWidget()
                .horizontalSizing(Sizing.fixed(200))
                .id(this.getSliderId(id));
        return this.getRow(id, tooltipId, true, slider);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Component getImageRow(String id) {
        Component textField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(200))
                .id(this.getImageValueFieldId(id));

        ImageButtonWidget imageButton = new ImageButtonWidget();
        imageButton.setMessage(Text.translatable("fzmm.gui.button.loadImage"));
        imageButton.margins(Insets.right(20).withLeft(COMPONENT_DISTANCE));
        imageButton.id(this.getImageButtonId(id));
        return this.getRow(id, id, false, textField, imageButton);
    }

    public Component getEnumRow(String id) {
        Component enumWidget = new EnumWidget()
                .horizontalSizing(Sizing.fixed(200))
                .id(this.getEnumId(id));

        return this.getRow(id, id, true, enumWidget);
    }

    public Component getScreenTabRow(Enum<? extends IScreenTab> defaultTab) {
        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(28))
                .surface(Surface.VANILLA_TRANSLUCENT)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .margins(Insets.bottom(12));

        for (var tab : defaultTab.getClass().getEnumConstants()) {
            IScreenTab screenTab = (IScreenTab) tab;
            boolean active = tab != defaultTab;
            Text text = Text.translatable(this.getBaseTranslationKey() + "." + screenTab.getId());
            ButtonWidget button = Components.button(text, (Consumer<ButtonComponent>) buttonComponent -> {});

            button.id(this.getScreenTabButtonId(screenTab.getId()))
                    .margins(Insets.horizontal(2));
            button.active = active;

            rowLayout.child(button);
        }
        return Containers.horizontalScroll(Sizing.fill(100), Sizing.fixed(30), rowLayout);
    }

    public ScreenTabContainer getScreenTab(String id, Component... components) {
        ScreenTabContainer screenTabContainer = new ScreenTabContainer(Sizing.fill(100), Sizing.content(), false);
        screenTabContainer.id(this.getScreenTabId(id));

        id += "TabTitle";
        screenTabContainer.child(this.getRow(id, id, false));
        screenTabContainer.children(Arrays.stream(components).toList());

        return screenTabContainer;
    }

    public Component getRow(String id, String tooltipId, boolean hasResetButton, Component... components) {
        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(22))
                .child(this.getLabel(id, tooltipId))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .margins(Insets.bottom(6))
                .id(this.getRowId(id));

        FlowLayout rightComponentsLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.content(), Sizing.content())
                .verticalAlignment(VerticalAlignment.CENTER)
                .positioning(Positioning.relative(100, 0));

        for (var component : components)
            rightComponentsLayout.child(component);

        if (hasResetButton)
            rightComponentsLayout.child(this.getResetButton(id));

        return rowLayout.child(rightComponentsLayout);
    }

    public void selectScreenTab(FlowLayout rootComponent, IScreenTab selectedTab) {
        for (var tab : selectedTab.getClass().getEnumConstants()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, this.getScreenTabId(tab));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, this.getScreenTabButtonId(tab));

            if (screenTabContainer != null)
                screenTabContainer.setSelected(selectedTab == tab);

            if (screenTabButton != null)
                screenTabButton.active = true;
        }
    }

    public String getLabelId(String id) {
        return id + "-label";
    }

    public String getRowId(String id) {
        return id + "-row";
    }

    public String getButtonId(String id) {
        return id + "-button";
    }

    public String getEnumId(String id) {
        return id + "-enum-option";
    }

    public String getResetId(String id) {
        return id + "-reset-button";
    }

    public String getImageButtonId(String id) {
        return id + "-image-option";
    }

    public String getImageValueFieldId(String id) {
        return id + "-value-field";
    }

    public String getSliderId(String id) {
        return id + "-slider";
    }


    public String getTextFieldId(String id) {
        return id + "-text-box";
    }


    public String getNumberFieldId(String id) {
        return id + "-number-field";
    }

    public String getToggleButtonId(String id) {
        return id + "-toggle-button";
    }

    public String getScreenTabId(String id) {
        return id + "-screen-tab";
    }

    public String getScreenTabId(IScreenTab tab) {
        return this.getScreenTabId(tab.getId());
    }

    public String getScreenTabButtonId(String id) {
        return id + "-screen-tab-button";
    }

    public String getScreenTabButtonId(IScreenTab tab) {
        return this.getScreenTabButtonId(tab.getId());
    }

    public String getBaseTranslationKey() {
        return "fzmm.gui." + this.baseTranslationKey;
    }

    protected void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with detailsId '%s'", componentTagName, id));
    }


    static {
        //noinspection UnstableApiUsage
        UIParsing.registerFactory("toggle-button", element -> new ConfigToggleButton());
        UIParsing.registerFactory("number-slider", element -> new SliderWidget());
        //noinspection UnstableApiUsage
        UIParsing.registerFactory("text-option", element -> new ConfigTextBox());
        UIParsing.registerFactory("image-option", element -> new ImageButtonWidget());
        UIParsing.registerFactory("enum-option", element -> new EnumWidget());
        UIParsing.registerFactory("screen-tab", ScreenTabContainer::parse);
    }
}
