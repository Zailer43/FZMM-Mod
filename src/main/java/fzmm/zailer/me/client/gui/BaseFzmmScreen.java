package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.components.ScreenTabContainer;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.image.mode.IImageMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageSource;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

//TODO: refactor since this class is becoming unmaintainable
// and making hovering over a row change the background color,
// it's hard to tell which row is selected when the gui is very small
@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<FlowLayout> {
    private static final int NORMAL_WIDTH = 200;
    private static final int TEXT_FIELD_WIDTH = NORMAL_WIDTH - 2;
    public static final int COMPONENT_DISTANCE = 8;
    public static final int BUTTON_TEXT_PADDING = 8;
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

    }

    protected abstract void setupButtonsCallbacks(FlowLayout rootComponent);

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    public EnumWidget setupEnum(FlowLayout rootComponent, String id, Enum<? extends IMode> defaultValue, @Nullable ButtonWidget.PressAction callback) {
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
        return enumButton;
    }

    public void setupImageButton(FlowLayout rootComponent, String id, IImageSource defaultMode) {
        ImageButtonWidget imageButtonWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(id));
        ConfigTextBox imageValueField = rootComponent.childById(ConfigTextBox.class, this.getImageValueFieldId(id));

        this.checkNull(imageButtonWidget, "image-option", this.getImageButtonId(id));
        this.checkNull(imageValueField, "text-option", this.getImageValueFieldId(id));

        imageValueField.applyPredicate(defaultMode::predicate);
        imageButtonWidget.onPress(button -> imageButtonWidget.loadImage(imageValueField.getText()));
        imageButtonWidget.setSourceType(defaultMode);
        assert this.client != null;
        imageButtonWidget.horizontalSizing(Sizing.fixed(this.client.textRenderer.getWidth(imageButtonWidget.getMessage()) + BUTTON_TEXT_PADDING));
    }

    public SliderWidget setupSlider(FlowLayout rootComponent, String id, double defaultValue, double min,
                                    double max, Class<? extends Number> numberType, @Nullable Consumer<Double> callback) {
        SliderWidget numberSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(numberSlider, "number-slider", this.getSliderId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        numberSlider.valueType(numberType);
        numberSlider.onChanged(aDouble -> {
            double discreteValue = numberSlider.discreteValue();
            resetButton.active = discreteValue != defaultValue;
            if (callback != null)
                callback.accept(discreteValue);
        });
        numberSlider.min(min);
        numberSlider.max(max);
        numberSlider.setFromDiscreteValue(defaultValue);

        resetButton.onPress(button -> numberSlider.setFromDiscreteValue(defaultValue));
        return numberSlider;
    }

    public TextFieldWidget setupTextField(FlowLayout rootComponent, String id, String defaultValue) {
        return this.setupTextField(rootComponent, id, defaultValue, null);
    }

    public TextFieldWidget setupTextField(FlowLayout rootComponent, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        TextFieldWidget textField = rootComponent.childById(TextFieldWidget.class, this.getTextFieldId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(textField, "text-box", this.getTextFieldId(id));

        textField.setChangedListener(text -> {
            if (resetButton != null)
                resetButton.active = !textField.getText().equals(defaultValue);
            if (changedListener != null)
                changedListener.accept(text);
        });
        if (defaultValue.length() > 32)
            textField.setMaxLength(defaultValue.length());
        textField.setText(defaultValue);
        textField.setCursor(0);

        if (resetButton != null)
            resetButton.onPress(button -> textField.setText(defaultValue));
        return textField;
    }

    public ConfigTextBox setupColorField(FlowLayout rootComponent, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        ConfigTextBox colorField = this.setupTextBox(rootComponent, this.getColorFieldId(id), id, defaultValue, changedListener);
        colorField.inputPredicate(input -> input.matches("[0-9a-fA-F]{0,6}"));
        return colorField;
    }

    public ConfigTextBox setupNumberField(FlowLayout rootComponent, String id, double defaultValue, Class<? extends Number> numberType) {
        return this.setupNumberField(rootComponent, id, defaultValue, numberType, null);
    }

    public ConfigTextBox setupNumberField(FlowLayout rootComponent, String id, double defaultValue, Class<? extends Number> numberType, @Nullable Consumer<String> changedListener) {
        String defaultValueString = NumberReflection.isFloatingPointType(numberType) ? String.valueOf(defaultValue) : String.valueOf((int) defaultValue);
        ConfigTextBox numberBox = this.setupTextBox(rootComponent, this.getNumberFieldId(id), id, defaultValueString, changedListener, s -> {
            try {
                return !s.isBlank() && defaultValue == Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return false;
            }
        });
        numberBox.configureForNumber(numberType);
        return numberBox;
    }

    private ConfigTextBox setupTextBox(FlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener) {
        return this.setupTextBox(rootComponent, textBoxId, id, defaultValue, changedListener, defaultValue::equals);
    }

    private ConfigTextBox setupTextBox(FlowLayout rootComponent, String textBoxId, String id, String defaultValue, @Nullable Consumer<String> changedListener, Predicate<String> defaultPredicate) {
        ConfigTextBox textBox = rootComponent.childById(ConfigTextBox.class, textBoxId);
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(textBox, "text-option", textBoxId);
        this.checkNull(resetButton, "button", this.getResetId(id));

        textBox.setChangedListener(s -> {
            resetButton.active = !defaultPredicate.test(s);
            if (changedListener != null)
                changedListener.accept(s);
        });
        textBox.setText(defaultValue);
        textBox.setCursor(0);

        resetButton.onPress(button -> textBox.setText(defaultValue));
        return textBox;
    }

    public ConfigToggleButton setupBooleanButton(FlowLayout rootComponent, String id, boolean defaultValue) {
        return this.setupBooleanButton(rootComponent, id, defaultValue, null);
    }

    public ConfigToggleButton setupBooleanButton(FlowLayout rootComponent, String id, boolean defaultValue, @Nullable ButtonWidget.PressAction toggledListener) {
        ConfigToggleButton toggleButton = rootComponent.childById(ConfigToggleButton.class, this.getToggleButtonId(id));
        ButtonWidget resetButton = rootComponent.childById(ButtonWidget.class, this.getResetId(id));

        this.checkNull(toggleButton, "toggle-button", this.getToggleButtonId(id));
        this.checkNull(resetButton, "button", this.getResetId(id));

        toggleButton.enabled(defaultValue);
        toggleButton.onPress(button -> {
            resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue;
            if (toggledListener != null)
                toggledListener.onPress(button);
        });
        assert this.client != null;
        int maxWidth = Math.max(
                this.client.textRenderer.getWidth(Text.translatable("text.owo.config.boolean_toggle.enabled")),
                this.client.textRenderer.getWidth(Text.translatable("text.owo.config.boolean_toggle.disabled"))
        );
        toggleButton.horizontalSizing(Sizing.fixed(maxWidth + BUTTON_TEXT_PADDING));

        resetButton.onPress(button -> toggleButton.onPress());
        resetButton.active = ((boolean) toggleButton.parsedValue()) != defaultValue;
        return toggleButton;
    }

    public ButtonWidget setupButton(FlowLayout rootComponent, String rawId, boolean enabled, ButtonWidget.PressAction callback) {
        ButtonWidget button = rootComponent.childById(ButtonWidget.class, rawId);

        this.checkNull(button, "button", rawId);

        button.active = enabled;
        button.onPress(callback);
        return button;
    }

    @SuppressWarnings("ConstantConditions")
    public ImageButtonWidget setupImage(FlowLayout rootComponent, String imageButtonId, String imageEnumId, Enum<? extends IImageMode> defaultValue) {
        this.setupImageButton(rootComponent, imageButtonId, ((IImageMode) defaultValue).getSourceType());
        ImageButtonWidget imageWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(imageButtonId));
        this.setupEnum(rootComponent, imageEnumId, defaultValue, button -> {
            IImageMode mode = (IImageMode) ((EnumWidget) button).getValue();
            IImageSource sourceType = mode.getSourceType();
            imageWidget.setSourceType(sourceType);
            rootComponent.childById(ConfigTextBox.class, this.getImageValueFieldId(imageButtonId))
                    .applyPredicate(sourceType::predicate);
        });

        return imageWidget;
    }

    public void tryAddComponentList(FlowLayout rootComponent, String containerId, Component... components) {
        FlowLayout container = rootComponent.childById(FlowLayout.class, containerId);
        if (container != null)
            container.children(new ArrayList<>(Arrays.stream(components).toList()));
    }

    public Component newLabel(String id, String tooltipId, boolean isOption) {
        String baseTranslationKey = isOption ? this.getOptionBaseTranslationKey() : this.getTabTranslationKey();
        return Components
                .label(Text.translatable(baseTranslationKey + id))
                .tooltip(Text.translatable(baseTranslationKey + tooltipId + ".tooltip"))
                .margins(Insets.left(20))
                .id(this.getLabelId(id));
    }

    public Component newResetButton(String id) {
        return Components
                .button(Text.translatable("fzmm.gui.button.reset"), (Consumer<ButtonComponent>) buttonComponent -> {
                })
                .id(this.getResetId(id));
    }

    public Component newTextFieldRow(String id) {
        return this.newTextFieldRow(id, true);
    }

    public Component newTextFieldRow(String id, boolean hasResetButton) {
        Component textField = Components
                .textBox(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(this.getTextFieldId(id));
        return this.newRow(id, id, hasResetButton, textField);
    }

    public Component newTextBoxRow(String id) {
        Component textField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(this.getTextFieldId(id));

        return this.newRow(id, id, true, textField);
    }

    public Component newNumberRow(String id) {
        return this.newNumberRow(id, id);
    }

    public Component newNumberRow(String id, String tooltipId) {
        Component textField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(this.getNumberFieldId(id));

        return this.newRow(id, tooltipId, true, textField);
    }

    public Component newColorRow(String id) {
        Component textField = new ConfigTextBox()
                .horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH))
                .id(this.getColorFieldId(id));

        return this.newRow(id, id, true, textField);
    }

    public Component newBooleanRow(String id) {
        Component textField = new ConfigToggleButton()
                .id(this.getToggleButtonId(id));
        return this.newRow(id, id, true, textField);
    }

    public Component newSliderRow(String id, int decimalPlaces) {
        return this.newSliderRow(id, id, decimalPlaces);
    }

    public Component newSliderRow(String id, String tooltipId, int decimalPlaces) {
        Component slider = new SliderWidget()
                .decimalPlaces(decimalPlaces)
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH))
                .id(this.getSliderId(id));
        return this.newRow(id, tooltipId, true, slider);
    }

    public Component newImageRow(String id) {
        Component textField = new ConfigTextBox()
                .id(this.getImageValueFieldId(id));
        Text loadImageButtonText = Text.translatable("fzmm.gui.button.loadImage");

        ImageButtonWidget imageButton = new ImageButtonWidget();
        imageButton.setMessage(loadImageButtonText);
        imageButton.id(this.getImageButtonId(id));

        ButtonWidget resetButton = (ButtonWidget) this.newResetButton("");
        // so that it aligns with the other options
        textField.horizontalSizing(Sizing.fixed(TEXT_FIELD_WIDTH -
                this.textRenderer.getWidth(loadImageButtonText) +
                this.textRenderer.getWidth(resetButton.getMessage())
        ));
        return this.newRow(id, id, false, textField, imageButton);
    }

    public Component newEnumRow(String id) {
        Component enumWidget = new EnumWidget()
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH))
                .id(this.getEnumId(id));

        return this.newRow(id, id, true, enumWidget);
    }

    public Component newButtonRow(String id) {
        ButtonWidget resetButton = (ButtonWidget) this.newResetButton("");
        Component button = Components.button(Text.translatable(this.getOptionBaseTranslationKey() + id + ".button"),
                        (Consumer<ButtonComponent>) buttonComponent -> {})
                .horizontalSizing(Sizing.fixed(NORMAL_WIDTH + this.textRenderer.getWidth(resetButton.getMessage()) + COMPONENT_DISTANCE + BUTTON_TEXT_PADDING))
                .id(this.getButtonId(id));

        return this.newRow(id, id, false, button);
    }

    public Component newScreenTabRow(Enum<? extends IScreenTab> defaultTab) {
        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(28))
                .surface(Surface.VANILLA_TRANSLUCENT)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .margins(Insets.bottom(12));

        for (var tab : defaultTab.getClass().getEnumConstants()) {
            IScreenTab screenTab = (IScreenTab) tab;
            boolean active = tab != defaultTab;
            Text text = Text.translatable(this.getTabTranslationKey() + screenTab.getId());
            ButtonWidget button = Components.button(text, (Consumer<ButtonComponent>) buttonComponent -> {
            });

            button.id(this.getScreenTabButtonId(screenTab.getId()))
                    .margins(Insets.horizontal(2));
            button.active = active;

            rowLayout.child(button);
        }

        return rowLayout;
    }

    public ScreenTabContainer newScreenTab(String id, Component... components) {
        ScreenTabContainer screenTabContainer = new ScreenTabContainer(Sizing.fill(100), Sizing.content(), false);
        screenTabContainer.id(this.getScreenTabId(id));

        screenTabContainer.child(this.newRow(id, id, false));
        screenTabContainer.children(Arrays.stream(components).toList());

        return screenTabContainer;
    }

    public Component newRow(String id, String tooltipId, boolean hasResetButton, Component... components) {
        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(22))
                .child(this.newLabel(id, tooltipId, components.length != 0))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .margins(Insets.bottom(4))
                .id(this.getRowId(id));

        FlowLayout rightComponentsLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.content(), Sizing.content())
                .verticalAlignment(VerticalAlignment.CENTER)
                .positioning(Positioning.relative(100, 0));


        for (var component : components) {
            component.margins(Insets.left(COMPONENT_DISTANCE));
            rightComponentsLayout.child(component);
        }

        if (hasResetButton)
            rightComponentsLayout.child(this.newResetButton(id));

        List<Component> rightComponents = rightComponentsLayout.children();
        if (!rightComponents.isEmpty())
            rightComponents.get(rightComponents.size() - 1).margins(Insets.right(20).withLeft(COMPONENT_DISTANCE));


        return rowLayout.child(rightComponentsLayout);
    }

    public void selectScreenTab(FlowLayout rootComponent, IScreenTab selectedTab) {
        for (var tab : selectedTab.getClass().getEnumConstants()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, this.getScreenTabId(tab));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, this.getScreenTabButtonId(tab));

            if (screenTabContainer != null)
                screenTabContainer.setSelected(selectedTab == tab);

            if (screenTabButton != null)
                screenTabButton.active = tab != selectedTab;
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

    public String getColorFieldId(String id) {
        return id + "-color";
    }

    public String getScreenTabButtonId(IScreenTab tab) {
        return this.getScreenTabButtonId(tab.getId());
    }

    public String getBaseTranslationKey() {
        return "fzmm.gui." + this.baseTranslationKey;
    }

    public String getTabTranslationKey() {
        return this.getBaseTranslationKey() + ".tab.";
    }

    public String getOptionBaseTranslationKey() {
        return this.getBaseTranslationKey() + ".option.";
    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    protected void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component id '%s'", componentTagName, id));
    }


    static {
        UIParsing.registerFactory("toggle-button", element -> new ConfigToggleButton());
        UIParsing.registerFactory("number-slider", element -> new SliderWidget());
        UIParsing.registerFactory("text-option", element -> new ConfigTextBox());
        UIParsing.registerFactory("image-option", element -> new ImageButtonWidget());
        UIParsing.registerFactory("enum-option", element -> new EnumWidget());
        UIParsing.registerFactory("screen-tab", ScreenTabContainer::parse);
    }
}
