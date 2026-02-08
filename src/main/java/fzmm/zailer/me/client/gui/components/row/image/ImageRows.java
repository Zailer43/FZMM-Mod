package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ImageRows extends EFlowLayout {
    public static int TOTAL_HEIGHT = AbstractRow.TOTAL_HEIGHT * 2;

    public ImageRows(String baseTranslationKey, String buttonId, String imageModeId, boolean translate) {
        this(baseTranslationKey, buttonId, buttonId, imageModeId, imageModeId, translate);
    }

    public ImageRows(String baseTranslationKey, String buttonId, String buttonTooltipId, String imageModeId, String imageTooltipId, boolean translate) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT), Algorithm.HORIZONTAL);

        FlowLayout rowsLayout = EContainers.verticalFlow(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));

        rowsLayout.children(List.of(
                new ImageButtonRow(baseTranslationKey, buttonId, buttonTooltipId, translate).hoveredSurface(null),
                new AbstractRow(baseTranslationKey, imageModeId, imageTooltipId, false, translate) {

                    @Override
                    public UIComponent[] getComponents(String id, String tooltipId) {
                        return new UIComponent[]{EContainers.horizontalFlow(Sizing.content(), Sizing.content()).id(imageModeId + "-layout")};
                    }
                }.hoveredSurface(null)
        ));
        this.hoveredSurface(EStyles.DEFAULT_HOVERED);

        this.child(rowsLayout);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            context.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @SuppressWarnings("ConstantConditions")
    public static ImageRowsElements setup(EFlowLayout rootComponent, String buttonId, String imageModeId, ImageMode defaultValue) {
        ImageButtonRow.setup(rootComponent, buttonId, defaultValue.getImageGetter());
        ImageButtonComponent imageWidget = rootComponent.childById(ImageButtonComponent.class, ImageButtonRow.getImageButtonId(buttonId));
        SuggestionTextBox suggestionTextBox = rootComponent.childById(SuggestionTextBox.class, ImageButtonRow.getImageValueFieldId(buttonId));

        FlowLayout imageModeLayout = rootComponent.childByIdOrThrow(FlowLayout.class, imageModeId + "-layout");
        imageModeLayout.gap(4);
        AtomicReference<ImageMode> selectedMode = new AtomicReference<>(defaultValue);
        HashMap<ImageMode, EButtonComponent> imageModeButtons = new HashMap<>();
        
        for (var modeOption : ImageMode.values()) {
            EButtonComponent modeButton = EComponents.button(net.minecraft.network.chat.Component.translatable(modeOption.getTranslationKey()));
            modeButton.onPress(button -> {
                selectedMode.set(modeOption);

                for (var imageMode : ImageMode.values()) {
                    if (imageMode != modeOption)
                        imageModeButtons.get(imageMode).active = true;
                }

                button.active = false;

                IImageGetter imageGetter = modeOption.getImageGetter();
                imageWidget.setSourceType(imageGetter);

                ImageButtonRow.setupSuggestionTextBox(suggestionTextBox, imageGetter);
            });
            FlowLayout modeButtonLayout = EContainers.verticalFlow(Sizing.content(), Sizing.content());
            modeButtonLayout.tooltip(net.minecraft.network.chat.Component.translatable(modeOption.getTranslationKey() + ".tooltip"));
            modeButton.horizontalSizing(Sizing.fixed(20));
            modeButtonLayout.child(modeButton);
            imageModeButtons.put(modeOption, modeButton);
            imageModeLayout.child(modeButtonLayout);
        }

        imageModeButtons.get(defaultValue).onPress();
        suggestionTextBox.setSuggestionSelectedCallback(imageWidget::onPress);

        return new ImageRowsElements(imageWidget, suggestionTextBox, selectedMode, imageModeButtons);
    }

    public static ImageRows parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);

        String buttonId = AbstractRow.getId(element, "buttonId");
        String buttonTooltipId = AbstractRow.getTooltipId(element, buttonId, "buttonTooltipId");

        String imageModeId = AbstractRow.getId(element, "imageModeId");
        String imageModeTooltipId = AbstractRow.getTooltipId(element, imageModeId, "imageModeTooltipId");

        return new ImageRows(baseTranslationKey, buttonId, buttonTooltipId, imageModeId, imageModeTooltipId, true);
    }
}
