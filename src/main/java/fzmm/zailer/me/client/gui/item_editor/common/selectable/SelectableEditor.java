package fzmm.zailer.me.client.gui.item_editor.common.selectable;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class SelectableEditor<T extends Component> implements IItemEditorScreen {
    protected T previewComponent;
    private FlowLayout contentLayout;
    private LabelComponent currentPageLabel;
    protected int currentPage;

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        // preview
        FlowLayout previewLayout = editorLayout.childById(FlowLayout.class, "preview");
        this.previewComponent = this.emptyComponent();
        previewLayout.child(this.previewComponent);

        // content
        this.contentLayout = editorLayout.childById(FlowLayout.class, "selectable-content");
        BaseFzmmScreen.checkNull(contentLayout, "flow-layout", "selectable-content");
        int maxByPage = this.getMaxByPage();

        for (int i = 0; i < maxByPage; i++) {
            T component = this.emptyComponent();
            component.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.select(component);
                return true;
            });
            this.contentLayout.child(component);
        }

        // page buttons
        this.currentPageLabel = editorLayout.childById(LabelComponent.class, "page-label");
        BaseFzmmScreen.checkNull(this.currentPageLabel, "label", "page-label");

        ButtonComponent previousPageButton = editorLayout.childById(ButtonComponent.class, "previous-page");
        BaseFzmmScreen.checkNull(previousPageButton, "button", "previous-page");
        previousPageButton.onPress(previousPageButtonComponent -> this.setPage(this.currentPage - 1));

        ButtonComponent nextPageButton = editorLayout.childById(ButtonComponent.class, "next-page");
        BaseFzmmScreen.checkNull(nextPageButton, "button", "next-page");
        nextPageButton.onPress(nextPageButtonComponent -> this.setPage(this.currentPage + 1));

        this.currentPage = -1;
        this.setPage(0);

        return editorLayout;
    }

    protected abstract int getMaxByPage();

    protected abstract int getSelectableSize();

    protected abstract void select(T component);

    protected abstract T emptyComponent();

    protected void setPage(int page) {
        int maxByPage = this.getMaxByPage();
        int maxPage = this.getSelectableSize() / maxByPage;
        page = MathHelper.clamp(page, 0, maxPage);
        if (page == this.currentPage)
            return;

        this.currentPage = page;
        this.currentPageLabel.text(Text.translatable("fzmm.gui.itemEditor.filled_map.label.page",
                (this.currentPage + 1), (maxPage + 1)));

        int startIndex = this.currentPage * maxByPage;
        List<Component> children = this.contentLayout.children();
        for (int i = 0; i < maxByPage; i++) {
            int index = startIndex + i;
            this.updateComponent(children.get(i), index);
        }
    }

    protected abstract void updateComponent(Component component, int index);


}
