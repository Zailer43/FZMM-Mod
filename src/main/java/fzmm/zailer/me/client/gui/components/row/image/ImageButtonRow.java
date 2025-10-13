package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import fzmm.zailer.me.client.gui.components.image.source.IImageSuggestion;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class ImageButtonRow extends AbstractRow {

    public ImageButtonRow(String baseTranslationKey, String id, String tooltipId, boolean translate) {
        super(baseTranslationKey, id, tooltipId, false, translate);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        Text loadImageButtonText = Text.translatable("fzmm.gui.button.loadImage");

        ImageButtonComponent imageButton = new ImageButtonComponent();
        imageButton.setMessage(loadImageButtonText);
        imageButton.id(getImageButtonId(id));
        ButtonWidget resetButton = (ButtonWidget) getResetButton("");

        Sizing textFieldSizing = Sizing.fixed(
                TEXT_FIELD_WIDTH -
                        Math.abs(textRenderer.getWidth(loadImageButtonText) - textRenderer.getWidth(resetButton.getMessage()))
        );

        SuggestionTextBox textField = new SuggestionTextBox(textFieldSizing, SuggestionTextBox.SuggestionPosition.BOTTOM, 5);
        textField.id(getImageValueFieldId(id));
        textField.keyPress().subscribe((input) -> {
            if (input.isEnter()) {
                imageButton.onPress(new Click(0, 0,  new MouseInput(0, 0)));
                return true;
            }

            return false;
        });

        return new Component[]{
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

    public static void setup(EFlowLayout rootComponent, String id, IImageGetter defaultMode) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        ImageButtonComponent imageButtonComponent = rootComponent.childByIdOrThrow(ImageButtonComponent.class, getImageButtonId(id));
        SuggestionTextBox suggestionTextBox = rootComponent.childByIdOrThrow(SuggestionTextBox.class, getImageValueFieldId(id));

        imageButtonComponent.onPress(button -> imageButtonComponent.loadImage(suggestionTextBox.getText()));
        imageButtonComponent.setSourceType(defaultMode);
        imageButtonComponent.horizontalSizing(Sizing.fixed(textRenderer.getWidth(imageButtonComponent.getMessage()) + BaseFzmmScreen.BUTTON_TEXT_PADDING));

        setupSuggestionTextBox(suggestionTextBox, defaultMode);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void setupSuggestionTextBox(SuggestionTextBox suggestionTextBox, IImageGetter imageGetter) {
        if (imageGetter instanceof IImageLoaderFromText imageLoaderFromText) {
            suggestionTextBox.applyPredicate(imageLoaderFromText::predicate);
        }

        suggestionTextBox.setSuggestionProvider(imageGetter instanceof IImageSuggestion imageSuggestion ?
                imageSuggestion.getSuggestionProvider() :
                (context, builder) -> CompletableFuture.completedFuture(builder.build())
        );

        suggestionTextBox.setVisible(imageGetter.hasTextField());
    }
}
