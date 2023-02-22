package fzmm.zailer.me.client.gui.components.containers;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class VerticalGridLayout extends BaseParentComponent {
    private final List<Component> children;
    private final int maxColumns;
    private final int maxChildren;
    private final int componentsWidth;
    private final int componentsHeight;
    protected Size contentSize;
    protected int gap;

    protected VerticalGridLayout(Sizing horizontalSizing, Sizing verticalSizing, int maxColumns, int maxChildren, int componentsWidth, int componentsHeight) {
        super(horizontalSizing, verticalSizing);
        this.children = new ArrayList<>();
        this.maxColumns = maxColumns;
        this.maxChildren = maxChildren;
        this.componentsWidth = componentsWidth;
        this.componentsHeight = componentsHeight;
        this.contentSize = Size.zero();
        this.gap = 0;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.contentSize.width() + this.padding.get().right();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.contentSize.height() + this.padding.get().bottom();
    }

    public void layout(Size space) {
        int childrenSize = this.children.size();
        var childSpace = this.calculateChildSpace(space);
        for (var child : this.children) {
            child.inflate(childSpace);
        }

        int availableWidth = space.width() - this.padding.get().horizontal();
        int columnSize = Math.min((int) Math.floor(availableWidth / (float) this.componentsWidth), this.maxColumns);
        int rowSize = (int) Math.ceil(childrenSize / (float) columnSize);
        var mountingOffset = this.childMountingOffset();
        MutableInt layoutX = new MutableInt(this.x + mountingOffset.width() - this.gap);
        MutableInt layoutY = new MutableInt(this.y + mountingOffset.height());

        int totalGapWidth = Math.max(0, this.gap * columnSize - 1);
        int totalGapHeight = Math.max(0, this.gap * rowSize - 1);

        this.contentSize = Size.of(this.componentsWidth * columnSize + totalGapWidth, this.componentsHeight * rowSize + totalGapHeight);
        int startX = this.horizontalAlignment().align(this.contentSize.width(), availableWidth);
        int startY = this.verticalAlignment().align(this.contentSize.height(), space.height() - this.padding.get().vertical());

        boolean hasMaxSize = this.maxChildren > 0;
        for (int row = 0; row < rowSize; row++) {
            layoutX.setValue(this.x + mountingOffset.width());

            for (int column = 0; column < columnSize; column++) {
                int lastIndex = row * columnSize + column;
                if (hasMaxSize && lastIndex >= this.maxChildren)
                    break;
                if (lastIndex >= childrenSize)
                    break;

                this.mountChild(this.children.get(lastIndex), childSpace, child -> child.mount(
                        this,
                        layoutX.intValue() + startX,
                        layoutY.intValue() + startY
                ));

                layoutX.add(this.componentsWidth + this.gap);
            }

            layoutY.add(this.componentsHeight + this.gap);
        }
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, this.children);
    }

    @Override
    public ParentComponent removeChild(Component child) {
        child.dismount(DismountReason.REMOVED);
        this.children.remove(child);
        this.updateLayout();

        return this;
    }

    public VerticalGridLayout child(Component child) {
        this.children.add(child);
        this.updateLayout();

        return this;
    }

    public VerticalGridLayout children(List<? extends Component> children) {
        this.children.addAll(children);
        this.updateLayout();

        return this;
    }


    @Override
    public List<Component> children() {
        return new ArrayList<>(this.children);
    }

    public void clearChildren() {
        this.children.clear();
        this.updateLayout();
    }

    public int getMaxChildren() {
        return this.maxChildren;
    }

    public VerticalGridLayout gap(int gap) {
        this.gap = gap;
        return this;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "gap", UIParsing::parseSignedInt, this::gap);

        final var components = UIParsing
                .get(children, "children", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var child : components) {
            this.child(model.parseComponent(Component.class, child));
        }
    }

    public static VerticalGridLayout parse(Element element) {
        UIParsing.expectAttributes(element, "maxColumns", "componentsWidth", "componentsHeight");

        int maxSize = element.hasAttribute("maxChildren") ? UIParsing.parseUnsignedInt(element.getAttributeNode("maxChildren")) : -1;
        int maxColumns = UIParsing.parseUnsignedInt(element.getAttributeNode("maxColumns"));
        int componentsWidth = UIParsing.parseUnsignedInt(element.getAttributeNode("componentsWidth"));
        int componentsHeight = UIParsing.parseUnsignedInt(element.getAttributeNode("componentsHeight"));

        return new VerticalGridLayout(Sizing.fill(100), Sizing.content(), maxColumns, maxSize, componentsWidth, componentsHeight);
    }
}
