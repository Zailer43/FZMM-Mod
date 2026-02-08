package fzmm.zailer.me.utils.history;

import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class HistoryClipboard {
    private final Deque<IClipboardState> undoArray = new ArrayDeque<>();
    private final Deque<IClipboardState> redoArray = new ArrayDeque<>();
    private final EButtonComponent undoButton;
    private final EButtonComponent redoButton;
    private final Observable<IClipboardState> valueObservable;
    private final int maxSize;

    public HistoryClipboard(IClipboardState value, EButtonComponent undoButton, EButtonComponent redoButton, int maxSize) {
        this.undoButton = undoButton;
        this.redoButton = redoButton;
        this.valueObservable = Observable.of(value.copy());
        this.maxSize = maxSize;

        this.setupButton(undoButton, redoButton);
        this.updateButtons();
    }

    private void setupButton(EButtonComponent undoButton, EButtonComponent redoButton) {
        undoButton.onPress(buttonComponent -> this.undo());
        undoButton.tooltip(List.of(
                Component.translatable("fzmm.gui.button.clipboard.undo"),
                Component.empty(),
                Component.translatable("fzmm.gui.hotkey.single"),
                Component.translatable("fzmm.gui.hotkey.ctrl").append(" + Z") // this doesn't need to be translatable, right?
        ));

        redoButton.onPress(buttonComponent -> this.redo());
        redoButton.tooltip(List.of(
                Component.translatable("fzmm.gui.button.clipboard.redo"),
                Component.empty(),
                Component.translatable("fzmm.gui.hotkey.plural"),
                Component.translatable("fzmm.gui.hotkey.ctrl").append(" + Y"),
                Component.translatable("fzmm.gui.hotkey.ctrl").append(" + ").append(Component.translatable("fzmm.gui.hotkey.shift")).append(" + Z")
        ));
    }

    public boolean keyPressed(KeyEvent input) {
        if (!input.hasControlDown()) return false;

        if (input.key() == GLFW.GLFW_KEY_Z && !input.hasShiftDown()) return this.undo();

        if ((input.key() == GLFW.GLFW_KEY_Z && input.hasShiftDown()) || input.key() == GLFW.GLFW_KEY_Y) return this.redo();

        return false;
    }

    public void onChange(Consumer<IClipboardState> consumer) {
        this.valueObservable.observe(consumer);
    }

    public void change(IClipboardState value) {
        this.valueObservable.set(value.copy());
    }

    public void addUndo(IClipboardState newValue) {
        this.undoArray.push(newValue.copy());
        this.clearRedo();
        this.updateButtons();

        if (this.undoArray.size() > this.maxSize) {
            this.undoArray.removeLast();
        }
    }

    public boolean undo() {
        if (this.undoArray.isEmpty()) {
            return false;
        }

        this.redoArray.push(this.valueObservable.get());
        this.valueObservable.set(this.undoArray.pop().copy());
        this.updateButtons();
        return true;
    }

    public boolean redo() {
        if (this.redoArray.isEmpty()) {
            return false;
        }

        this.undoArray.push(this.valueObservable.get());
        this.valueObservable.set(this.redoArray.pop().copy());
        this.updateButtons();
        return true;
    }

    public void clearUndo() {
        this.undoArray.clear();
        this.undoButton.active(false);
        this.clearRedo();
    }

    public void clearRedo() {
        this.redoArray.clear();
        this.redoButton.active(false);
    }

    public boolean hasUndo() {
        return !this.undoArray.isEmpty();
    }

    public boolean hasRedo() {
        return !this.redoArray.isEmpty();
    }

    private void updateButtons() {
        this.undoButton.active(this.hasUndo());
        this.redoButton.active(this.hasRedo());
    }
}
