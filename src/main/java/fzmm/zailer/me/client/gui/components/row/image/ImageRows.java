package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ImageRows extends FlowLayout {
    public static int TOTAL_HEIGHT = AbstractRow.TOTAL_HEIGHT * 2;

    public ImageRows(String baseTranslationKey, String buttonId, String buttonTooltipId, String imageModeId, String imageTooltipId, boolean translate) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT), Algorithm.HORIZONTAL);

        FlowLayout rowsLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));

        rowsLayout.children(List.of(
                new ImageButtonRow(baseTranslationKey, buttonId, buttonTooltipId, translate)
                        .setHasHoveredBackground(false),
                new AbstractRow(baseTranslationKey, imageModeId, imageTooltipId, false, translate) {

                    @Override
                    public Component[] getComponents(String id, String tooltipId) {
                        return new Component[]{Containers.horizontalFlow(Sizing.content(), Sizing.content()).id(imageModeId + "-layout")};
                    }
                }.setHasHoveredBackground(false)
        ));

        this.child(rowsLayout);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            context.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    @SuppressWarnings("ConstantConditions")
    public static ImageRowsElements setup(FlowLayout rootComponent, String buttonId, String imageModeId, ImageMode defaultValue) {
        ImageButtonRow.setup(rootComponent, buttonId, defaultValue.getImageGetter());
        ImageButtonComponent imageWidget = rootComponent.childById(ImageButtonComponent.class, ImageButtonRow.getImageButtonId(buttonId));
        SuggestionTextBox suggestionTextBox = rootComponent.childById(SuggestionTextBox.class, ImageButtonRow.getImageValueFieldId(buttonId));

        FlowLayout imageModeLayout = rootComponent.childById(FlowLayout.class, imageModeId + "-layout");
        BaseFzmmScreen.checkNull(imageModeLayout, "flow-layout", imageModeId + "-layout");
        imageModeLayout.gap(4);
        AtomicReference<ImageMode> selectedMode = new AtomicReference<>(defaultValue);
        HashMap<ImageMode, ButtonComponent> imageModeButtons = new HashMap<>();
        
        for (var modeOption : ImageMode.values()) {
            ButtonComponent modeButton = Components.button(Text.translatable(modeOption.getTranslationKey()), button -> {
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
            
            modeButton.tooltip(Text.translatable(modeOption.getTranslationKey() + ".tooltip"));
            modeButton.horizontalSizing(Sizing.fixed(20));
            imageModeButtons.put(modeOption, modeButton);
            imageModeLayout.child(modeButton);
        }

        imageModeButtons.get(defaultValue).onPress();

        return new ImageRowsElements(imageWidget, suggestionTextBox.getTextBox(), selectedMode, imageModeButtons, suggestionTextBox);
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
