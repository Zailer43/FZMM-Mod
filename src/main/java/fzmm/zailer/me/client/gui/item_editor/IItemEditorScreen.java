package fzmm.zailer.me.client.gui.item_editor;

import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public interface IItemEditorScreen {


    List<RequestedItem> getRequestedItems();

    default boolean isApplicable(ItemStack stack) {
        if (this.getRequestedItems().isEmpty())
            return true;

        return this.getRequestedItems().stream().anyMatch(requestedItem -> requestedItem.predicate().test(stack));
    }

    ItemStack getExampleItem();

    default Text getTitle() {
        return Text.translatable("fzmm.gui.itemEditor." + this.getId() + ".title");
    }

    FlowLayout getLayout(ItemEditorBaseScreen baseScreen, int x, int y, int width, int height);

    String getId();

    /**
     * update the preview of the item in the requested item
     */
    void updateItemPreview();

    /**
     * set the first item of {@link IItemEditorScreen#getRequestedItems()}
     */
    void selectItemAndUpdateParameters(ItemStack stack);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);
}