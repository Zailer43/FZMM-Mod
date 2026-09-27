package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.components.containers.SelectableLayout;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxMenuActions;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxMenuManager;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HistoryScreen extends BaseFzmmScreen {

    private static final Component GENERATED_ITEMS_EMPTY_TEXT = net.minecraft.network.chat.Component.translatable("fzmm.gui.history.label.generatedWithFzmm.empty");
    private EButtonComponent itemGenerated;
    private EButtonComponent headGenerated;
    private SelectableLayout contentLayout;
    private ELabelComponent labelError;

    public HistoryScreen(@Nullable Screen parent) {
        super("history", "history", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.contentLayout = rootComponent.childById(SelectableLayout.class, "content");
        this.contentLayout.contextMenu(new CtxMenuManager<>(CtxMenuActions.itemActions(), CtxMenuManager::parseItems));

        this.itemGenerated = rootComponent.childByIdOrThrow(EButtonComponent.class, "itemGeneratedWithFzmm");
        this.itemGenerated.onPress(this::itemGeneratedExecute);
        this.headGenerated = rootComponent.childByIdOrThrow(EButtonComponent.class, "headGeneratedWithFzmm");
        this.headGenerated.onPress(this::headGeneratedExecute);

        this.labelError = rootComponent.childByIdOrThrow(ELabelComponent.class, "error-label");

        this.itemGenerated.onPress();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return this.contentLayout.onKeyPress(input) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.contentLayout.onCharTyped(input) || super.charTyped(input);
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
        this.contentLayout.<SelectableLayout>configure(layout -> {
            layout.clearChildren();
            layout.children(stackList.stream().map(itemStack -> (UIComponent) EComponents.itemGive(itemStack)).toList());
        });
        this.labelError.text(stackList.isEmpty() ? GENERATED_ITEMS_EMPTY_TEXT : net.minecraft.network.chat.Component.empty());
    }


}
