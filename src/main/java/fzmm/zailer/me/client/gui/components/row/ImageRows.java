package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.mode.IImageMode;
import fzmm.zailer.me.client.gui.components.image.source.IImageGetter;
import fzmm.zailer.me.client.gui.components.image.source.IImageLoaderFromText;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;

import java.util.List;

public class ImageRows extends HorizontalFlowLayout {
    public static int TOTAL_HEIGHT = AbstractRow.TOTAL_HEIGHT * 2;

    public ImageRows(String baseTranslationKey, String buttonId, String buttonTooltipId, String enumId, String enumTooltipId) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));

        FlowLayout rowsLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));

        rowsLayout.children(List.of(
                new ImageButtonRow(baseTranslationKey, buttonId, buttonTooltipId).setHasHoveredBackground(false),
                new EnumRow(baseTranslationKey, enumId, enumTooltipId).setHasHoveredBackground(false)
        ));

        this.child(rowsLayout);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    public static ImageButtonComponent setup(FlowLayout rootComponent, String buttonId, String enumId, Enum<? extends IImageMode> defaultValue) {
        ImageButtonRow.setup(rootComponent, buttonId, ((IImageMode) defaultValue).getImageGetter());
        ImageButtonComponent imageWidget = rootComponent.childById(ImageButtonComponent.class, ImageButtonRow.getImageButtonId(buttonId));

        EnumRow.setup(rootComponent, enumId, defaultValue, button -> {
            IImageMode mode = (IImageMode) ((EnumWidget) button).getValue();
            IImageGetter imageGetter = mode.getImageGetter();
            imageWidget.setSourceType(imageGetter);
            ConfigTextBox imageValueField = rootComponent.childById(ConfigTextBox.class, ImageButtonRow.getImageValueFieldId(buttonId));

            if (imageGetter instanceof IImageLoaderFromText imageLoaderFromText)
                imageValueField.applyPredicate(imageLoaderFromText::predicate);

            imageValueField.visible = imageGetter.hasTextField();
        });

        return imageWidget;
    }

    public static ImageRows parse(Element element) {
        String baseTranslationKey = AbstractRow.getBaseTranslationKey(element);

        String buttonId = AbstractRow.getId(element, "buttonId");
        String buttonTooltipId = AbstractRow.getTooltipId(element, buttonId, "buttonTooltipId");

        String enumId = AbstractRow.getId(element, "enumId");
        String enumTooltipId = AbstractRow.getTooltipId(element, enumId, "enumTooltipId");

        return new ImageRows(baseTranslationKey, buttonId, buttonTooltipId, enumId, enumTooltipId);
    }
}
