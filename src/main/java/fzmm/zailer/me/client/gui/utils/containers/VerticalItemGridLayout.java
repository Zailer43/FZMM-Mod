package fzmm.zailer.me.client.gui.utils.containers;

import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.apache.commons.lang3.mutable.MutableInt;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

public class VerticalItemGridLayout extends GridLayout {
    private static final int ITEM_COMPONENT_SIZE = 16;
    private final int maxSize;

    protected VerticalItemGridLayout(Sizing verticalSizing, int maxColumns, int maxSize) {
        super(Sizing.fill(100), verticalSizing, (int) Math.ceil(maxSize / (float) maxColumns), maxColumns);
        this.maxSize = maxSize;
    }

    public void layout(Size space) {
        var childSpace = this.calculateChildSpace(space);
        for (var child : this.children) {
            if (child != null) {
                child.inflate(childSpace);
            }
        }

        int availableWidth = space.width() - this.padding.get().horizontal();
        int columnSize = Math.min((int) Math.floor(availableWidth / (float) ITEM_COMPONENT_SIZE), this.columns);
        int rowSize = (int) Math.ceil(this.maxSize / (float) columnSize) + 1;
        var mountingOffset = this.childMountingOffset();
        MutableInt layoutX = new MutableInt(this.x + mountingOffset.width());
        MutableInt layoutY = new MutableInt(this.y + mountingOffset.height());

        this.contentSize = Size.of(ITEM_COMPONENT_SIZE * columnSize, ITEM_COMPONENT_SIZE * rowSize);
        int startX = this.horizontalAlignment().align(this.contentSize.width(), availableWidth);
        int startY = this.verticalAlignment().align(this.contentSize.height(), space.height() - this.padding.get().vertical());

        for (int row = 0; row < rowSize; row++) {
            layoutX.setValue(this.x + mountingOffset.width());

            for (int column = 0; column < columnSize; column++) {
                int lastIndex = row * columnSize + column;
                if (lastIndex >= this.maxSize)
                    break;

                this.mountChild(this.children[lastIndex], childSpace, child -> child.mount(
                        this,
                        layoutX.intValue() + startX,
                        layoutY.intValue() + startY
                ));

                layoutX.add(ITEM_COMPONENT_SIZE);
            }

            layoutY.add(ITEM_COMPONENT_SIZE);
        }
    }

    public GridLayout child(Component child) {
        int lastElementIndex = 0;
        for (int i = 0; i != this.children.length; i++) {
            lastElementIndex = i;
            if (this.children[i] == null) {
                break;
            }
        }

        this.children[lastElementIndex] = child;
        this.nonNullChildren.add(child);
        this.updateLayout();

        return this;
    }

    public GridLayout children(List<? extends Component> children) {
        int lastElementIndex = 0;
        for (int i = 0; i != this.children.length; i++) {
            lastElementIndex = i;
            if (this.children[i] == null) {
                break;
            }
        }

        for (int i = 0; i != children.size(); i++) {
            Component child = children.get(i);
            this.children[lastElementIndex + i] = child;
            this.nonNullChildren.add(child);
        }

        this.updateLayout();

        return this;
    }

    public void clearChildren() {
        Arrays.fill(this.children, null);
        this.nonNullChildren.clear();
        this.updateLayout();
    }

    public int getMaxSize() {
        return this.maxSize;
    }


    public static GridLayout parse(Element element) {
        UIParsing.expectAttributes(element, "maxSize", "maxColumns");

        int maxSize = UIParsing.parseUnsignedInt(element.getAttributeNode("maxSize"));
        int maxColumns = UIParsing.parseUnsignedInt(element.getAttributeNode("maxColumns"));

        return new VerticalItemGridLayout(Sizing.content(), maxColumns, maxSize);
    }
}
