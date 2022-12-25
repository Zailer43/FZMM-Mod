package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.image.source.IImageSource;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ImageButtonRow extends AbstractRow {

    public ImageButtonRow(String baseTranslationKey, String id, String tooltipId) {
        super(baseTranslationKey, id, tooltipId, false);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Component[] getComponents(String id, String tooltipId) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Component textField = new ConfigTextBox()
                .id(getImageValueFieldId(id));
        Text loadImageButtonText = Text.translatable("fzmm.gui.button.loadImage");

        ImageButtonWidget imageButton = new ImageButtonWidget();
        imageButton.setMessage(loadImageButtonText);
        imageButton.id(getImageButtonId(id));

        ButtonWidget resetButton = (ButtonWidget) getResetButton("");

        // so that it aligns with the other options
        textField.horizontalSizing(Sizing.fixed(
                TEXT_FIELD_WIDTH -
                textRenderer.getWidth(loadImageButtonText) +
                textRenderer.getWidth(resetButton.getMessage())
        ));

        return new Component[] {
                textField,
                imageButton
        };
    }

    public static String getImageButtonId(String id) {
        return id + "-image-option";
    }

    public static String getImageValueFieldId(String id) {
        return id + "-value-field";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void setup(FlowLayout rootComponent, String id, IImageSource defaultMode) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ImageButtonWidget imageButtonWidget = rootComponent.childById(ImageButtonWidget.class, getImageButtonId(id));
        ConfigTextBox imageValueField = rootComponent.childById(ConfigTextBox.class, getImageValueFieldId(id));

        BaseFzmmScreen.checkNull(imageButtonWidget, "image-option", getImageButtonId(id));
        BaseFzmmScreen.checkNull(imageValueField, "text-option", getImageValueFieldId(id));

        imageValueField.applyPredicate(defaultMode::predicate);
        imageButtonWidget.onPress(button -> imageButtonWidget.loadImage(imageValueField.getText()));
        imageButtonWidget.setSourceType(defaultMode);
        imageButtonWidget.horizontalSizing(Sizing.fixed(textRenderer.getWidth(imageButtonWidget.getMessage()) + BaseFzmmScreen.BUTTON_TEXT_PADDING));
    }
}
