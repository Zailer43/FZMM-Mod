package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
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

        ImageButtonComponent imageButton = new ImageButtonComponent();
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
    public static void setup(FlowLayout rootComponent, String id, IImageGetter defaultMode) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ImageButtonComponent imageButtonComponent = rootComponent.childById(ImageButtonComponent.class, getImageButtonId(id));
        ConfigTextBox imageValueField = rootComponent.childById(ConfigTextBox.class, getImageValueFieldId(id));

        BaseFzmmScreen.checkNull(imageButtonComponent, "image-option", getImageButtonId(id));
        BaseFzmmScreen.checkNull(imageValueField, "text-option", getImageValueFieldId(id));

        imageButtonComponent.onPress(button -> imageButtonComponent.loadImage(imageValueField.getText()));
        imageButtonComponent.setSourceType(defaultMode);
        imageButtonComponent.horizontalSizing(Sizing.fixed(textRenderer.getWidth(imageButtonComponent.getMessage()) + BaseFzmmScreen.BUTTON_TEXT_PADDING));
        
        if (defaultMode instanceof  IImageLoaderFromText imageLoaderFromText)
            imageValueField.applyPredicate(imageLoaderFromText::predicate);

        imageValueField.visible = defaultMode.hasTextField();
    }
}
