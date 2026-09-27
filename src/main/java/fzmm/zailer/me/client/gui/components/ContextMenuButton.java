package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxElement;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxMenuManager;
import fzmm.zailer.me.client.gui.utils.context_menu.CtxType;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ContextMenuButton extends EButtonComponent {
    protected CtxMenuManager<?> contextMenu = null;

    public ContextMenuButton(Component text) {
        super(text, button -> {
        });
        this.verticalSizing(Sizing.fixed(20));
    }

    /**
     * @param entries Button entries require the mode to be set to CtxEntry#none
     */
    public void entries(List<CtxElement<Object>> entries) {
        this.contextMenu = new CtxMenuManager<>(entries, o -> null);
    }

    public void removeContextMenu() {
        this.contextMenu.clearChildren();
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Screen screen = Minecraft.getInstance().gui.screen();
        if (!(screen instanceof BaseFzmmScreen baseScreen)) return;

        if (this.contextMenu.isUnmounted()) {
            this.contextMenu.createLayout(baseScreen.root(), false);
            this.contextMenu.configureNode(layout -> layout.horizontalSizing(Sizing.fixed(this.width())));
            this.contextMenu.initAt(List.of(), CtxType.UI, this.x(), this.y() + this.height());
            this.contextMenu.focus();
        } else {
            this.removeContextMenu();
        }
    }
}
