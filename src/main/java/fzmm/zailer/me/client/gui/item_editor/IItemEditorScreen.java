package fzmm.zailer.me.client.gui.item_editor;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

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

    default Optional<FlowLayout> getLayoutModel(int x, int y, int width, int height) {
        Optional<UIModel> uiModel = this.getUIModel();

        if (uiModel.isEmpty()) {
            FzmmClient.LOGGER.error("[IItemEditorScreen] Failed to load UIModel");
            return Optional.empty();
        }

        return Optional.of(uiModel.get().createAdapterWithoutScreen(x, y, width, height, FlowLayout.class).rootComponent);
    }

    default Optional<UIModel> getUIModel() {
        return Optional.ofNullable(BaseUIModelScreen.DataSource.asset(new Identifier(FzmmClient.MOD_ID, this.getUIModelPath())).get());
    }

    default String getUIModelPath() {
        return "item_editor/" + this.getId() + "_editor";
    }

    FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout);

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