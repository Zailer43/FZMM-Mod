package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.components.GiveItemComponent;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.logic.FzmmHistory;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
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
    private static final String ERROR_LABEL_ID = "error-label";
    private ButtonComponent itemGenerated;
    private ButtonComponent headGenerated;
    private FlowLayout contentLayout;
    private LabelComponent labelError;


    public HistoryScreen(@Nullable Screen parent) {
        super("history", "history", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);

        this.itemGenerated = ButtonRow.setup(rootComponent, ITEM_GENERATED_ID, true, this::itemGeneratedExecute);
        this.headGenerated = ButtonRow.setup(rootComponent, HEAD_GENERATED_ID, true, this::headGeneratedExecute);

        this.labelError = rootComponent.childById(LabelComponent.class, ERROR_LABEL_ID);
        checkNull(this.labelError, "label", ERROR_LABEL_ID);

        this.itemGenerated.onPress();
    }

    private void itemGeneratedExecute(ButtonComponent button) {
        this.addItems(FzmmHistory.getGeneratedItems());
        this.selectOption(button);
    }

    private void headGeneratedExecute(ButtonComponent button) {
        this.addItems(FzmmHistory.getGeneratedHeads());
        this.selectOption(button);
    }

    private void selectOption(ButtonComponent button) {
        this.itemGenerated.active = this.itemGenerated != button;
        this.headGenerated.active = this.headGenerated != button;
    }

    private void addItems(List<ItemStack> stackList) {
        this.contentLayout.clearChildren();
        this.contentLayout.children(stackList.stream().map(itemStack -> (Component) new GiveItemComponent(itemStack)).toList());
        this.labelError.text(stackList.isEmpty() ? GENERATED_ITEMS_EMPTY_TEXT : Text.empty());
    }


}
