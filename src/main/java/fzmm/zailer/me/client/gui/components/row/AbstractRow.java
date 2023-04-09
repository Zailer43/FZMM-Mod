package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Optional;

public abstract class AbstractRow extends FlowLayout {
    protected static final int NORMAL_WIDTH = 200;
    protected static final int TEXT_FIELD_WIDTH = NORMAL_WIDTH - 2;
    public static final int ROW_HEIGHT = 22;
    public static final int VERTICAL_MARGIN = 2;
    public static final int TOTAL_HEIGHT = ROW_HEIGHT + VERTICAL_MARGIN * 2;
    protected final String baseTranslationKey;
    private boolean hasHoveredBackground;
    private String id;
    private final boolean translate;

    public AbstractRow(String baseTranslationKey) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT), Algorithm.HORIZONTAL);
        this.baseTranslationKey = baseTranslationKey;
        this.hasHoveredBackground = true;
        this.translate = true;
    }

    public AbstractRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton) {
        this(baseTranslationKey, id, tooltipId, hasResetButton, true);
    }

    public AbstractRow(String baseTranslationKey, String id, String tooltipId, boolean hasResetButton, boolean translate) {
        super(Sizing.fill(100), Sizing.fixed(TOTAL_HEIGHT), Algorithm.HORIZONTAL);
        this.baseTranslationKey = baseTranslationKey;
        this.hasHoveredBackground = true;
        this.id = id;
        this.translate = translate;
        Component[] components = this.getComponents(id, tooltipId);

        FlowLayout rowLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.fill(100), Sizing.fixed(ROW_HEIGHT))
                .child(this.getLabel(id, tooltipId, components.length != 0))
                .gap(BaseFzmmScreen.COMPONENT_DISTANCE)
                .alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
                .margins(Insets.vertical(VERTICAL_MARGIN))
                .id(getRowContainerId(id));

        FlowLayout rightComponentsLayout = (FlowLayout) Containers
                .horizontalFlow(Sizing.content(), Sizing.fill(100))
                .gap(BaseFzmmScreen.COMPONENT_DISTANCE)
                .verticalAlignment(VerticalAlignment.CENTER)
                .positioning(Positioning.relative(100, 0))
                .id(getRightLayoutId(id));


        for (var component : components)
            rightComponentsLayout.child(component);

        if (hasResetButton)
            rightComponentsLayout.child(this.getResetButton(id));

        List<Component> rightComponents = rightComponentsLayout.children();
        if (!rightComponents.isEmpty())
            rightComponents.get(rightComponents.size() - 1).margins(Insets.right(20));

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
        String baseTranslationKey = isOption ? BaseFzmmScreen.getOptionBaseTranslationKey(this.baseTranslationKey) : BaseFzmmScreen.getTabTranslationKey(this.baseTranslationKey);
        return getLabel(id, tooltipId, baseTranslationKey, this.translate);
    }

    public static Component getLabel(String id, String tooltipId, String baseTranslationKey, boolean translate) {
        LabelComponent label = (LabelComponent) Components.label(translate ? Text.translatable(baseTranslationKey + id) : Text.literal(id))
                .margins(Insets.left(20))
                .id(getLabelId(id));

        if (translate)
            label.tooltip(Text.translatable(baseTranslationKey + tooltipId + ".tooltip"));

        return label;
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

    public static String getRightLayoutId(String id) {
        return id + "-right-layout";
    }

    public static String getId(Element element) {
        return getId(element, "id");
    }

    public static String getId(Element element, String id) {
        return UIParsing.parseText(UIParsing.childElements(element).get(id)).getString();
    }

    public static String getTooltipId(Element element, String defaultValue) {
        return getTooltipId(element, defaultValue, "tooltipId");
    }

    public static String getTooltipId(Element element, String defaultValue, String id) {
        boolean containsTooltipId = UIParsing.childElements(element).containsKey(id);
        return containsTooltipId ? UIParsing.parseText(UIParsing.childElements(element).get(id)).getString() : defaultValue;
    }

    public AbstractRow setHasHoveredBackground(boolean hasHoveredBackground) {
        this.hasHoveredBackground = hasHoveredBackground;
        return this;
    }

    public void removeResetButton() {
        FlowLayout rightLayout = this.childById(FlowLayout.class, getRightLayoutId(this.id));
        if (rightLayout == null)
            return;

        ButtonComponent resetButton = rightLayout.childById(ButtonComponent.class, getResetButtonId(this.id));

        if (resetButton == null)
            return;

        rightLayout.removeChild(resetButton);
    }

    public void removeHorizontalMargins() {
        Optional<FlowLayout> rightLayoutOptional = this.getRightLayout();
        if (rightLayoutOptional.isPresent() && rightLayoutOptional.get().children().size() > 0) {
            List<Component> rightLayoutChildren = rightLayoutOptional.get().children();
            Component lastElement = rightLayoutChildren.get(rightLayoutChildren.size() - 1);
            Insets previousMargins = lastElement.margins().get();
            lastElement.margins(previousMargins.withRight(0));
        }

        LabelComponent label = this.childById(LabelComponent.class, getLabelId(id));
        if (label != null)
            label.margins(Insets.left(0));
    }

    public Optional<FlowLayout> getRightLayout() {
        return Optional.ofNullable(this.childById(FlowLayout.class, getRightLayoutId(this.id)));
    }

    public Optional<FlowLayout> getRowContainer() {
        return Optional.ofNullable(this.childById(FlowLayout.class, getRowContainerId(this.id)));
    }

    protected String getId() {
        return this.id;
    }
}