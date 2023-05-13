package fzmm.zailer.me.client.gui.components.row.image;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.mode.IImageMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;

import java.util.List;

public class ImageRows extends FlowLayout {
    public static int TOTAL_HEIGHT = AbstractRow.TOTAL_HEIGHT * 2;

    public ImageRows(String baseTranslationKey, String buttonId, String buttonTooltipId, String enumId, String enumTooltipId, boolean translate) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT), Algorithm.HORIZONTAL);

        FlowLayout rowsLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));

        rowsLayout.children(List.of(
                new ImageButtonRow(baseTranslationKey, buttonId, buttonTooltipId, translate)
                        .setHasHoveredBackground(false),
                new EnumRow(baseTranslationKey, enumId, enumTooltipId, translate)
                        .setHasHoveredBackground(false)
        ));

        this.child(rowsLayout);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    @SuppressWarnings("ConstantConditions")
    public static ImageRowsElements setup(FlowLayout rootComponent, String buttonId, String enumId, Enum<? extends IImageMode> defaultValue) {
        ImageButtonRow.setup(rootComponent, buttonId, ((IImageMode) defaultValue).getImageGetter());
        ImageButtonComponent imageWidget = rootComponent.childById(ImageButtonComponent.class, ImageButtonRow.getImageButtonId(buttonId));
        SuggestionTextBox suggestionTextBox = rootComponent.childById(SuggestionTextBox.class, ImageButtonRow.getImageValueFieldId(buttonId));

        EnumWidget enumMode = EnumRow.setup(rootComponent, enumId, defaultValue, true, button -> {
            IImageMode mode = (IImageMode) ((EnumWidget) button).getValue();
            IImageGetter imageGetter = mode.getImageGetter();
            imageWidget.setSourceType(imageGetter);

            ImageButtonRow.setupSuggestionTextBox(suggestionTextBox, imageGetter);
        });

        return new ImageRowsElements(imageWidget, suggestionTextBox.getTextBox(), enumMode, suggestionTextBox);
    }

    public static ImageRows parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);

        String buttonId = AbstractRow.getId(element, "buttonId");
        String buttonTooltipId = AbstractRow.getTooltipId(element, buttonId, "buttonTooltipId");

        String enumId = AbstractRow.getId(element, "enumId");
        String enumTooltipId = AbstractRow.getTooltipId(element, enumId, "enumTooltipId");

        return new ImageRows(baseTranslationKey, buttonId, buttonTooltipId, enumId, enumTooltipId, true);
    }
}
