package fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class AppliedLevelableComponent<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>>  extends BaseLevelableComponent<V, D, B> {
    private final Runnable callback;
    protected ConfigTextBox textBox;
    protected boolean updateItemCallback;

    public AppliedLevelableComponent(D levelable, @Nullable Runnable callback,
                                     LevelableEditor<V, D, B> editor, B builder) {
        super(levelable, editor, builder);
        this.callback = callback;
        this.updateItemCallback = true;
    }

    @Override
    protected List<? extends Component> getOptions() {
        ConfigTextBox textBox = new ConfigTextBox().configureForNumber(Short.class);
        textBox.setText(String.valueOf(this.getLevelable().getLevel()));
        textBox.horizontalSizing(Sizing.fixed(40));
        textBox.setCursorToStart(false);
        this.textBox = textBox;

        this.textBox.onChanged().subscribe(value -> {
            int parsedValue = this.getLevel();
            this.getLevelable().setLevel(parsedValue);
            if (this.updateItemCallback)
                this.editor.updateItemPreview();
        });

        return List.of(this.textBox);
    }

    @Override
    protected List<ButtonComponent> getButtons(LevelableEditor<V, D, B> editor, B builder) {
        ButtonComponent removeButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.remove"), button -> {
            if (this.isDisabled)
                return;

            int index = this.getLevelableIndex(editor);

            if (index >= 0) {
                builder.remove(index);
                editor.updateItemPreview();

                editor.getAppliedLevelablesLayout().removeChild(this);

                if (this.callback != null)
                    this.callback.run();
            }
        }).horizontalSizing(Sizing.fixed(20));

        return List.of(removeButton);
    }

    /**
     * @return the index of the levelable in the list, -1 if not found
     */
    public int getLevelableIndex(LevelableEditor<V, D, B> editor) {
        List<Component> children = editor.getAppliedLevelablesLayout().children();

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == this) {
                return i;
            }
        }

        return -1;
    }

    public void setLevel(String value) {
        this.textBox.text(value);
        this.textBox.setCursorToStart(false);
    }

    public int getLevel() {
        return (short) this.textBox.parsedValue();
    }

    public void setUpdateItemCallback(boolean updateItemCallback) {
        this.updateItemCallback = updateItemCallback;
    }
}
