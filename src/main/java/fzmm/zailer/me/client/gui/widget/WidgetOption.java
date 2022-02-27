package fzmm.zailer.me.client.gui.widget;

import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.gui.*;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.render.RenderUtils;
import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.enums.CustomConfigType;
import fzmm.zailer.me.client.gui.enums.FzmmIcons;
import fzmm.zailer.me.client.gui.interfaces.ICustomOption;
import fzmm.zailer.me.client.gui.options.ImageOption;
import fzmm.zailer.me.client.gui.options.ImageOption.LoadImageListener;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class WidgetOption extends WidgetConfigOptionBase<OptionWrapper> {
    private final OptionWrapper wrapper;
    private CustomConfigType customConfigType;
    private int colorDisplayPosX;
    private int imageStatusPosX;

    public WidgetOption(int x, int y, int width, int height, int labelWidth, int configWidth, OptionWrapper wrapper, int listIndex, WidgetListConfigOptionsBase<?, ?> parent) {
        super(x, y, width, height, parent, wrapper, listIndex);
        this.wrapper = wrapper;

        if (wrapper.getType() == OptionWrapper.Type.OPTION) {
            if (wrapper.getConfig() != null && wrapper.getConfig() instanceof ICustomOption customConfig)
                this.customConfigType = customConfig.getConfigType();
            this.addConfigOption(x, y, labelWidth, configWidth);
        } else {
            this.customConfigType = null;
            this.initialStringValue = null;
            this.lastAppliedValue = null;

            this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, wrapper.getLabel());
        }
    }

    private void addConfigOption(int x, int y, int labelWidth, int configWidth) {
        IConfigBase config = this.wrapper.getConfig();
        if (config == null)
            return;

        y += 1;
        int configHeight = 20;

        this.addLabel(x, y + 7, labelWidth, 8, 0xFFFFFFFF, config.getConfigGuiDisplayName());

        String comment = config.getComment();

        if (comment != null)
            this.addWidget(new WidgetHoverInfo(x, y + 5, labelWidth, 12, comment));

        x += labelWidth + 10;

        if (this.customConfigType != null) {

            if (this.customConfigType == CustomConfigType.IMAGE) {
                int resetX = x + configWidth + 2;
                this.addImageOptionEntry(x, y, resetX, configWidth, configHeight, (ImageOption) config);
            }
        } else {
            ConfigType type = config.getType();

            switch (type) {
                case BOOLEAN -> {
                    ConfigButtonBoolean optionButton = new ConfigButtonBoolean(x, y, configWidth, configHeight, (IConfigBoolean) config);
                    this.addOptionButtonEntry(x + configWidth + 2, y, (IConfigResettable) config, optionButton);
                }
                case OPTION_LIST -> {
                    ConfigButtonOptionList optionButton = new ConfigButtonOptionList(x, y, configWidth, configHeight, (IConfigOptionList) config);
                    this.addConfigButtonEntry(x + configWidth + 2, y, (IConfigResettable) config, optionButton);
                }
                case STRING, COLOR, INTEGER, DOUBLE -> {
                    int resetX = x + configWidth + 2;
                    if (type == ConfigType.COLOR) {
                        configWidth -= 22; // adjust the width to match other configs due to the color display
                        this.colorDisplayPosX = x + configWidth + 2;
                    } else if (type == ConfigType.INTEGER || type == ConfigType.DOUBLE) {
                        configWidth -= 18;
                        this.colorDisplayPosX = x + configWidth + 2;
                    }
                    if ((type == ConfigType.INTEGER || type == ConfigType.DOUBLE) && config instanceof IConfigSlider configSlider && configSlider.shouldUseSlider()) {
                        this.addOptionSliderEntry(x, y, resetX, configWidth, configHeight, configSlider);
                    } else {
                        this.addOptionTextFieldEntry(x, y, resetX, configWidth, configHeight, (IConfigValue) config);
                    }
                    if (config instanceof IConfigSlider) {
                        IGuiIcon icon = ((IConfigSlider) config).shouldUseSlider() ? MaLiLibIcons.BTN_TXTFIELD : MaLiLibIcons.BTN_SLIDER;
                        ButtonGeneric toggleBtn = new ButtonGeneric(this.colorDisplayPosX, y + 2, icon);
                        this.addButton(toggleBtn, new WidgetConfigOption.ListenerSliderToggle((IConfigSlider) config));
                    }
                }
            }
        }

    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {
        RenderUtils.color(1f, 1f, 1f, 1f);

        this.drawSubWidgets(mouseX, mouseY, matrixStack);

        if (this.wrapper.getType() == OptionWrapper.Type.OPTION) {
            IConfigBase config = this.wrapper.getConfig();
            assert config != null;

            this.drawTextFields(mouseX, mouseY, matrixStack);
            super.render(mouseX, mouseY, selected, matrixStack);

            if (config.getType() == ConfigType.COLOR) {
                int y = this.y + 1;
                RenderUtils.drawRect(this.colorDisplayPosX, y, 19, 19, 0xFFFFFFFF);
                RenderUtils.drawRect(this.colorDisplayPosX + 1, y + 1, 17, 17, 0xFF000000);
                RenderUtils.drawRect(this.colorDisplayPosX + 2, y + 2, 15, 15, 0xFF000000 | ((ConfigColor) config).getIntegerValue());
            } else if (this.customConfigType != null && this.customConfigType == CustomConfigType.IMAGE) {
                ImageOption imageConfig = (ImageOption) config;
                Text text = new LiteralText(imageConfig.getStatusMessage());
                FzmmIcons icon = imageConfig.getStatusIcon();
                icon.renderAt(this.imageStatusPosX, y + 2, this.zLevel);
                this.textRenderer.draw(matrixStack, text, this.imageStatusPosX + 18, y + 6, 0xFFFFFFFF);
            }
        }
    }

    private void addOptionButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton) {
        ButtonGeneric resetButton = this.createResetButton(xReset, yReset, config);
        ConfigOptionChangeListenerButton listenerChange = new ConfigOptionChangeListenerButton(config, resetButton, null);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigOptionListenerResetConfig.ConfigResetterButton(optionButton), resetButton, null);

        this.addButton(optionButton, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    private void addOptionTextFieldEntry(int x, int y, int resetX, int configWidth, int configHeight, IConfigValue config) {
        GuiTextFieldGeneric field = this.createTextField(x, y + 1, configWidth - 4, configHeight - 3);
        field.setMaxLength(this.maxTextfieldTextLength);
        field.setText(config.getStringValue());

        ButtonGeneric resetButton = this.createResetButton(resetX, y, config);
        ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(config, field, resetButton);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigOptionListenerResetConfig.ConfigResetterTextField(config, field), resetButton, null);

        this.addTextField(field, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    private void addOptionSliderEntry(int x, int y, int resetX, int configWidth, int configHeight, IConfigSlider config) {
        ButtonGeneric resetButton = this.createResetButton(resetX, y, config);
        ISliderCallback callback;

        if (config instanceof IConfigDouble) {
            callback = new SliderCallbackDouble((IConfigDouble) config, resetButton);
        } else if (config instanceof IConfigInteger) {
            callback = new SliderCallbackInteger((IConfigInteger) config, resetButton);
        } else {
            return;
        }

        WidgetSlider slider = new WidgetSlider(x, y, configWidth, configHeight, callback);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, null, resetButton, null);

        this.addWidget(slider);
        this.addButton(resetButton, listenerReset);
    }

    private void addConfigButtonEntry(int xReset, int yReset, IConfigResettable config, ButtonBase optionButton) {
        ButtonGeneric resetButton = this.createResetButton(xReset, yReset, config);
        ConfigOptionChangeListenerButton listenerChange = new ConfigOptionChangeListenerButton(config, resetButton, null);
        ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(config, new ConfigOptionListenerResetConfig.ConfigResetterButton(optionButton), resetButton, null);

        this.addButton(optionButton, listenerChange);
        this.addButton(resetButton, listenerReset);
    }

    private void addImageOptionEntry(int x, int y, int xButtons, int configWidth, int configHeight, ImageOption config) {
        ConfigButtonOptionList changeModeButton = new ConfigButtonOptionList(xButtons, y, config.getModeWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, config.mode);
        int changeModeWidth = changeModeButton.getWidth();
        changeModeButton.setX(xButtons - changeModeWidth - 2);

        GuiTextFieldGeneric field = this.createTextField(x, y + 1, configWidth - changeModeWidth - 6, configHeight - 3);
        field.setMaxLength(this.maxTextfieldTextLength);
        field.setText(config.getStringValue());

        ButtonGeneric loadButton = new ButtonGeneric(xButtons, y, -1, ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.LOAD_IMAGE.getText());
        loadButton.setEnabled(config.isModified());

        this.imageStatusPosX = xButtons + loadButton.getWidth() + 2;

        ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(config, field, loadButton);
        LoadImageListener listenerLoad = new LoadImageListener(config);

        this.addTextField(field, listenerChange);
        this.addButton(changeModeButton, null);
        this.addButton(loadButton, listenerLoad);

    }

    @Override
    public boolean wasConfigModified() {
        return false;
    }

    @Override
    public void applyNewValueToConfig() {
        if (this.wrapper.getType() == OptionWrapper.Type.OPTION && this.wrapper.getConfig() instanceof IStringRepresentable config) {
            if (this.textField != null && this.hasPendingModifications())
                config.setValueFromString(this.textField.getTextField().getText());

            this.lastAppliedValue = config.getStringValue();
        }
    }

}
