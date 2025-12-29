package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ContextMenuButton extends EButtonComponent {
    @Nullable
    private DropdownComponent contextMenu = null;
    private Consumer<DropdownComponent> contextMenuOptionsConsumer = dropdownComponent -> {
    };

    public ContextMenuButton(net.minecraft.network.chat.Component text) {
        super(text, button -> {
        });
        this.verticalSizing(Sizing.fixed(20));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof BaseFzmmScreen baseScreen)) {
            return;
        }

        if ((this.contextMenu == null || !this.contextMenu.hasParent())) {
            DropdownComponent.openContextMenu(baseScreen, baseScreen.root(), FlowLayout::child,
                    this.x(), this.y(), contextMenu -> {
                        this.contextMenu = contextMenu;
                        this.contextMenuOptionsConsumer.accept(contextMenu);

                        List<UIComponent> dropdownChildren = contextMenu.children();


                        // workaround, since there is no dismount event in owo-lib,
                        // so when the button is clicked, it dismounts because the
                        // click happens outside, and then it opens again
                        int contextMenuY = this.height();
                        contextMenu.padding(Insets.top(contextMenuY));
                        contextMenu.cursorStyle(CursorStyle.HAND);
                        if (!dropdownChildren.isEmpty()) {
                            dropdownChildren.get(0)
                                    .horizontalSizing(Sizing.fixed(this.width()))
                                    .cursorStyle(CursorStyle.NONE);
                        }

                        contextMenu.mouseDown().subscribe((contextInput, doubled) -> {
                            if (contextInput.y() < contextMenuY) {
                                baseScreen.root().removeChild(contextMenu);
                                UISounds.playButtonSound();
                            }

                            // fixes that if you click on the margins zone it clicks on the component behind the dropdown
                            return true;
                        });
                    });
        } else {
            this.removeContextMenu();
        }
    }

    public void setContextMenuOptions(Consumer<DropdownComponent> contextMenuOptionsConsumer) {
        this.contextMenuOptionsConsumer = contextMenuOptionsConsumer;
    }

    public void removeContextMenu() {
        if (this.contextMenu != null) {
            this.contextMenu.remove();
            this.contextMenu = null;
        }
    }
}
