package fzmm.zailer.me.client.gui.components.extend.component;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class EBooleanButton extends EButtonComponent {

    protected boolean enabled = false;
    protected final Component enabledText;
    protected final Component disabledText;

    public EBooleanButton(Component text, Color enabledColor) {
        super(Component.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.enabledText = text.copy().setStyle(Style.EMPTY.withColor(enabledColor.rgb()).withItalic(true));
        this.disabledText = text.copy().setStyle(Style.EMPTY);
        this.updateMessage();
    }

    public EBooleanButton(Component enabledText, Component disabledText) {
        super(Component.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.enabledText = enabledText;
        this.disabledText = disabledText;
        this.updateMessage();
    }

    @SuppressWarnings("NoTranslation")
    public EBooleanButton() {
        this(Component.translatable("text.owo.config.boolean_toggle.enabled"), Component.translatable("text.owo.config.boolean_toggle.disabled"));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.enabled = !this.enabled;
        this.updateMessage();
        super.onPress(input);
    }

    protected void updateMessage() {
        this.setMessage(this.enabled ? this.enabledText : this.disabledText);
    }

    public void enabled(boolean enabled) {
        this.enabledIgnoreCallback(enabled);
        this.onPress.onPress(this);
    }

    public void enabledIgnoreCallback(boolean enabled) {
        this.enabled = enabled;
        this.updateMessage();
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setContentHorizontalSizing() {
        int maxWidth = FzmmUtils.getMaxWidth(List.of(this.enabledText, this.disabledText)) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.horizontalSizing(Sizing.fixed(maxWidth));
    }

    public static EBooleanButton parse(Element element) {
        Map<String, Element> children = UIParsing.childElements(element);

        if (children.containsKey("text")) {
            Component text = UIParsing.parseText(children.get("text"));
            Color enabledColor = Color.parse(children.get("enabled-color"));
            return new EBooleanButton(text, enabledColor);
        }
        Component enabledText = UIParsing.parseText(children.get("enabled-text"));
        Component disabledText = UIParsing.parseText(children.get("disabled-text"));
        return new EBooleanButton(enabledText, disabledText);
    }
    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::setMessage);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
        UIParsing.apply(children, "renderer", Renderer::parse, this::renderer);
    }
}
