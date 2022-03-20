package fzmm.zailer.me.mixin.malilib;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IStringRepresentable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonOptionList;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.enums.CustomConfigType;
import fzmm.zailer.me.client.gui.enums.FzmmIcons;
import fzmm.zailer.me.client.gui.interfaces.ICustomOption;
import fzmm.zailer.me.client.gui.options.ImageOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WidgetConfigOption.class)
public class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    private int imageStatusPosX;

    @Shadow(remap = false) @Final protected GuiConfigsBase.ConfigOptionWrapper wrapper;

    public WidgetConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
        this.imageStatusPosX = 0;
    }

    @Inject(method = "addConfigOption", at = @At(value = "JUMP", ordinal = 3), remap = false, cancellable = true)
    private void addConfigOption(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci) {
        if (this.isCustomConfig()) {
            int configHeight = 20;

            if (((ICustomOption) config).getConfigType() == CustomConfigType.IMAGE) {
                int resetX = x + configWidth + 2;
                this.addImageOptionEntry(x, y, resetX, configWidth, configHeight, (ImageOption) config);
            }
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void render(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack, CallbackInfo ci) {
        if (this.isCustomConfig()) {
            ICustomOption config = (ICustomOption) this.wrapper.getConfig();
            assert config != null;

            if (config.getConfigType() == CustomConfigType.IMAGE) {
                ImageOption imageConfig = (ImageOption) config;
                Text text = new LiteralText(imageConfig.getStatusMessage());
                FzmmIcons icon = imageConfig.getStatusIcon();
                icon.renderAt(this.imageStatusPosX, y + 2, this.zLevel);
                this.textRenderer.draw(matrixStack, text, this.imageStatusPosX + 18, y + 6, 0xFFFFFFFF);
            }
        }
    }

    private void addImageOptionEntry(int x, int y, int xButtons, int configWidth, int configHeight, ImageOption config) {
        ConfigButtonOptionList changeModeButton = new ConfigButtonOptionList(xButtons, y, config.getModeWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, config.mode);
        int changeModeWidth = changeModeButton.getWidth();
        changeModeButton.setX(xButtons - changeModeWidth - 2);

        GuiTextFieldGeneric field = this.createTextField(x, y + 1, configWidth - changeModeWidth - 6, configHeight - 3);
        field.setMaxLength(this.maxTextfieldTextLength);
        field.setText(config.getStringValue());

        ButtonGeneric loadButton = Buttons.LOAD_IMAGE.get(xButtons, y);
        loadButton.setEnabled(config.isModified());

        this.imageStatusPosX = xButtons + loadButton.getWidth() + 2;

        ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(config, field, loadButton);
        ImageOption.LoadImageListener listenerLoad = new ImageOption.LoadImageListener(config);

        this.addTextField(field, listenerChange);
        this.addButton(changeModeButton, null);
        this.addButton(loadButton, listenerLoad);

    }

    private boolean isCustomConfig() {
        return wrapper.getConfig() != null && wrapper.getConfig() instanceof ICustomOption;
    }

    @Override
    public boolean wasConfigModified() {
        return false;
    }

    @Override
    public void applyNewValueToConfig() {
        if (this.wrapper.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG &&
                this.wrapper.getConfig() instanceof IStringRepresentable config) {
            if (this.textField != null && this.hasPendingModifications())
                config.setValueFromString(this.textField.getTextField().getText());

            this.lastAppliedValue = config.getStringValue();
        }
    }
}
