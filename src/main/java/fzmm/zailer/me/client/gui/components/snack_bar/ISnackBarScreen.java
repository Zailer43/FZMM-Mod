package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public interface ISnackBarScreen {

    ParentUIComponent root();

    FlowLayout getSnackBarLayout();

    default void addSnackBar(ISnackBarComponent snackBar) {
        SnackBarManager.getInstance().add(snackBar).removeOverflow();
    }

    default void addSnackBar(List<ISnackBarComponent> snackBarList) {
        SnackBarManager.getInstance().add(snackBarList).removeOverflow();
    }

    default void removeSnackBar(String id) {
        SnackBarManager.getInstance().remove(id);
    }

    default void removeSnackBar(ISnackBarComponent snackBar) {
        SnackBarManager.getInstance().remove(snackBar);
    }

    /**
     * Add the overlay below the snack bar
     */
    default void addOverlay(UIComponent overlay) {
        FlowLayout snackBarLayout = this.getSnackBarLayout();
        ParentUIComponent root = this.root();

        if (root instanceof FlowLayout rootLayout) {
            rootLayout.child(overlay);
            // snack bar layout is always the last child
            rootLayout.removeChild(snackBarLayout);
            rootLayout.child(snackBarLayout);
        }
    }

    default void clearSnackBars() {
        this.getSnackBarLayout().clearChildren();
    }

    default List<ISnackBarComponent> getSnackBars() {
        return this.getSnackBarLayout().children()
                .stream()
                .filter(component -> component instanceof ISnackBarComponent)
                .map(component -> (ISnackBarComponent) component)
                .collect(Collectors.toList());
    }

    default void setScreen(@Nullable Screen screen) {
        Minecraft client = Minecraft.getInstance();

        if (screen == null && client.gui.screen() == null) {
            return;
        }

        SnackBarManager manager = SnackBarManager.getInstance();

        if (!(screen instanceof ISnackBarScreen snackBarScreen)) {
            manager.moveToHud(this);
        } else if (client.gui.screen() == null) {
            manager.moveToScreen(snackBarScreen);
        } else {
            manager.move(this, snackBarScreen);
        }

        client.gui.setScreen(screen);
    }
}
