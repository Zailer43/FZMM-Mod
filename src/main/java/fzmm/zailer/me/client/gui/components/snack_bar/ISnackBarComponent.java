package fzmm.zailer.me.client.gui.components.snack_bar;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.UIComponent;

import java.util.List;


public interface ISnackBarComponent extends ParentUIComponent {

    void setTimer(long timerMillis);
    
    void updateTimer(long timeElapsed);

    void updateTimerBar(float percent);

    ISnackBarComponent startTimer();

    boolean removeOnLimit();

    void removeOnLimit(boolean value);

    default void close() {
        ParentUIComponent parent = this.parent();
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    void add(UIComponent snackBar);

    void setButtons(List<ButtonComponent> buttons);

    void buttonsEnabled(boolean value);
}
