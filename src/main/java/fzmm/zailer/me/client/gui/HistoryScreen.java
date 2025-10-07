package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
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
    private ButtonComponent itemGenerated;
    private ButtonComponent headGenerated;
    private FlowLayout contentLayout;
    private LabelComponent labelError;


    public HistoryScreen(@Nullable Screen parent) {
        super("history", "history", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.contentLayout = rootComponent.childById(FlowLayout.class, "content");

        this.itemGenerated = rootComponent.childByIdOrThrow(ButtonComponent.class, "itemGeneratedWithFzmm").onPress(this::itemGeneratedExecute);
        this.headGenerated = rootComponent.childByIdOrThrow(ButtonComponent.class, "headGeneratedWithFzmm").onPress(this::headGeneratedExecute);

        this.labelError = rootComponent.childByIdOrThrow(LabelComponent.class, "error-label");

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
        this.contentLayout.children(stackList.stream().map(itemStack -> (Component) EComponents.itemGive(itemStack)).toList());
        this.labelError.text(stackList.isEmpty() ? GENERATED_ITEMS_EMPTY_TEXT : Text.empty());
    }


}
