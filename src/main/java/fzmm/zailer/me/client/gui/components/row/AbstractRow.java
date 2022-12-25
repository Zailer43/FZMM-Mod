package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.List;

public abstract class AbstractRow extends HorizontalFlowLayout {
    protected static final int NORMAL_WIDTH = 200;
    protected static final int TEXT_FIELD_WIDTH = NORMAL_WIDTH - 2;
    public static final int ROW_HEIGHT = 22;
    public static final int VERTICAL_MARGIN = 2;
    public static final int TOTAL_HEIGHT = ROW_HEIGHT + VERTICAL_MARGIN * 2;
    protected final String baseTranslationKey;
    private boolean hasHoveredBackground;

    public AbstractRow(String baseTranslationKey) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));
        this.baseTranslationKey = baseTranslationKey;
        this.hasHoveredBackground = true;
    }
    public AbstractRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT));
        this.baseTranslationKey = baseTranslationKey;
        this.hasHoveredBackground = true;
        Component[] components = this.getComponents(id, tooltipId);

        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(ROW_HEIGHT))
                .child(this.getLabel(id, tooltipId, components.length != 0))
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .margins(Insets.vertical(VERTICAL_MARGIN))
                .id(getRowContainerId(id));

        FlowLayout rightComponentsLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.content(), Sizing.fill(100))
                .verticalAlignment(VerticalAlignment.CENTER)
                .positioning(Positioning.relative(100, 0));


        for (var component : components) {
            component.margins(Insets.left(BaseFzmmScreen.COMPONENT_DISTANCE));
            rightComponentsLayout.child(component);
        }

        if (hasResetButton)
            rightComponentsLayout.child(this.getResetButton(id));

        List<Component> rightComponents = rightComponentsLayout.children();
        if (!rightComponents.isEmpty())
            rightComponents.get(rightComponents.size() - 1).margins(Insets.right(20).withLeft(BaseFzmmScreen.COMPONENT_DISTANCE));

        this.child(rowLayout.child(rightComponentsLayout));
    }

    public abstract Component[] getComponents(String id, String tooltipId);

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered && this.hasHoveredBackground)
            Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    public Component getResetButton(String id) {
        return Components
                .button(Text.translatable("fzmm.gui.button.reset"), buttonComponent -> {
                })
                .id(getResetButtonId(id));
    }

    public Component getLabel(String id, String tooltipId, boolean isOption) {
        String baseTranslationKey = isOption ? this.getOptionBaseTranslationKey() : this.getTabTranslationKey();
        return Components
                .label(Text.translatable(baseTranslationKey + id))
                .tooltip(Text.translatable(baseTranslationKey + tooltipId + ".tooltip"))
                .margins(Insets.left(20))
                .id(getLabelId(id));

    }

    public static String getRowContainerId(String id) {
        return id + "-row";
    }

    public static String getResetButtonId(String id) {
        return id + "-reset-button";
    }

    public static String getLabelId(String id) {
        return id + "-label";
    }

    public String getBaseTranslationKey() {
        return "fzmm.gui." + this.baseTranslationKey;
    }

    public String getTabTranslationKey() {
        return this.getBaseTranslationKey() + ".tab.";
    }

    public String getOptionBaseTranslationKey() {
        return this.getBaseTranslationKey() + ".option.";
    }

    public static String getBaseTranslationKey(Element element) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        return currentScreen instanceof BaseFzmmScreen baseFzmmScreen ? baseFzmmScreen.getBaseScreenTranslationKey() : element.getAttribute("baseScreenTranslationKey");
    }

    public static String getId(Element element) {
        return getId(element, "id");
    }
    public static String getId(Element element, String id) {
        return UIParsing.parseText(UIParsing.childElements(element).get(id)).getString();
    }

    public static String getTooltipId(Element element, String defaultValue) {
        return getTooltipId(element, defaultValue, "tooltipId" );
    }

    public static String getTooltipId(Element element, String defaultValue, String id) {
        boolean containsTooltipId = UIParsing.childElements(element).containsKey(id);
        return containsTooltipId ? UIParsing.parseText(UIParsing.childElements(element).get(id)).getString() : defaultValue;
    }

    public AbstractRow setHasHoveredBackground(boolean hasHoveredBackground) {
        this.hasHoveredBackground = hasHoveredBackground;
        return this;
    }
}