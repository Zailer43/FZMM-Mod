package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import fzmm.zailer.me.client.gui.components.image.source.IImageSuggestion;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.concurrent.CompletableFuture;

public class ImageButtonRow extends AbstractRow {

    public ImageButtonRow(String baseTranslationKey, String id, String tooltipId, boolean translate) {
        super(baseTranslationKey, id, tooltipId, false, translate);
    }

    @Override
    public UIComponent[] getComponents(String id, String tooltipId) {
        Font textRenderer = Minecraft.getInstance().font;

        net.minecraft.network.chat.Component loadImageButtonText = net.minecraft.network.chat.Component.translatable("fzmm.gui.button.loadImage");

        ImageButtonComponent imageButton = new ImageButtonComponent();
        imageButton.setMessage(loadImageButtonText);
        imageButton.id(getImageButtonId(id));
        Button resetButton = (Button) getResetButton("");

        Sizing textFieldSizing = Sizing.fixed(
                TEXT_FIELD_WIDTH -
                        Math.abs(textRenderer.width(loadImageButtonText) - textRenderer.width(resetButton.getMessage()))
        );

        SuggestionTextBox textField = new SuggestionTextBox(textFieldSizing, SuggestionTextBox.SuggestionPosition.BOTTOM, 5);
        textField.id(getImageValueFieldId(id));
        textField.keyPress().subscribe((input) -> {
            if (input.isConfirmation()) {
                imageButton.onPress(new MouseButtonEvent(0, 0,  new MouseButtonInfo(0, 0)));
                return true;
            }

            return false;
        });

        return new UIComponent[]{
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
        Font textRenderer = Minecraft.getInstance().font;
        ImageButtonComponent imageButtonComponent = rootComponent.childByIdOrThrow(ImageButtonComponent.class, getImageButtonId(id));
        SuggestionTextBox suggestionTextBox = rootComponent.childByIdOrThrow(SuggestionTextBox.class, getImageValueFieldId(id));

        imageButtonComponent.onPress(button -> imageButtonComponent.loadImage(suggestionTextBox.getValue()));
        imageButtonComponent.setSourceType(defaultMode);
        imageButtonComponent.horizontalSizing(Sizing.fixed(textRenderer.width(imageButtonComponent.getMessage()) + BaseFzmmScreen.BUTTON_TEXT_PADDING));

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
