package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.utils.components.GiveItemComponent;
import fzmm.zailer.me.client.logic.FzmmHistory;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HistoryScreen extends BaseFzmmScreen {

    private static final Text GENERATED_ITEMS_EMPTY_TEXT = Text.translatable("fzmm.gui.history.label.generatedWithFzmm.empty");
    private static final String CONTENT_ID = "content";
    private static final String ITEM_GENERATED_ID = "itemGeneratedWithFzmm";
    private static final String HEAD_GENERATED_ID = "headGeneratedWithFzmm";
    private ButtonComponent itemGenerated;
    private ButtonComponent headGenerated;


    public HistoryScreen(@Nullable Screen parent) {
        super("history", "history", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FlowLayout contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(contentLayout, "flow-layout", CONTENT_ID);

        this.itemGenerated = ButtonRow.setup(rootComponent, ITEM_GENERATED_ID, true, buttonComponent -> this.itemGeneratedExecute(buttonComponent, contentLayout));
        this.headGenerated = ButtonRow.setup(rootComponent, HEAD_GENERATED_ID, true, buttonComponent -> this.headGeneratedExecute(buttonComponent, contentLayout));

        this.itemGenerated.onPress();
    }

    private void itemGeneratedExecute(ButtonComponent button, FlowLayout contentLayout) {
        contentLayout.clearChildren();
        contentLayout.child(this.getItemGrid(FzmmHistory.getGeneratedItems()));
        this.selectOption(button);
    }

    private void headGeneratedExecute(ButtonComponent button, FlowLayout contentLayout) {
        contentLayout.clearChildren();
        contentLayout.child(this.getItemGrid(FzmmHistory.getGeneratedHeads()));
        this.selectOption(button);
    }

    private void selectOption(ButtonComponent button) {
        this.itemGenerated.active = this.itemGenerated != button;
        this.headGenerated.active = this.headGenerated != button;
    }

    private Component getItemGrid(List<ItemStack> stackList) {
        int columns = FzmmClient.CONFIG.history.itemGridColumns();
        if (stackList.size() == 0)
            return Components.label(GENERATED_ITEMS_EMPTY_TEXT);

        GridLayout itemGrid = Containers.grid(Sizing.content(), Sizing.content(), stackList.size() / columns + 1, columns);

        for (int i = 0; i != stackList.size(); i++) {
            ItemComponent itemComponent = new GiveItemComponent(stackList.get(i));
            itemGrid.child(itemComponent, i / columns, i % columns);
        }

        return itemGrid;
    }


}
