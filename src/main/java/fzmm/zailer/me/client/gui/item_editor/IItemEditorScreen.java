package fzmm.zailer.me.client.gui.item_editor;

import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public interface IItemEditorScreen {

    default List<RequestedItem> getRequestedItems() {
        return this.getRequestedItems(itemStack -> {});
    }

    List<RequestedItem> getRequestedItems(Consumer<ItemStack> firstItemSetter);

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
     * set the first item of {@link IItemEditorScreen#getRequestedItems()}
     */
    void setItem(ItemStack stack);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);
}