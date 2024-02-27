package fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AddLevelableComponent<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> extends BaseLevelableComponent<V, D, B> {
    protected ButtonComponent button;
    @Nullable
    protected final Runnable callback;
    protected boolean useCallback;

    public AddLevelableComponent(D levelable, @Nullable Runnable callback,
                                 LevelableEditor<V, D, B> editor, B builder) {
        super(levelable, editor, builder);
        this.callback = callback;
        this.useCallback = true;
    }

    @Override
    protected List<? extends Component> getOptions() {
        return new ArrayList<>();
    }

    @Override
    protected List<ButtonComponent> getButtons(LevelableEditor<V, D, B> editor, B builder) {
        this.button = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.add"), button -> {
            if (this.isDisabled)
                return;
            builder.add(this.getLevelable());
            if (this.useCallback)
                editor.updateItemPreview();

            if (!editor.isAllowDuplicates()) {
                editor.getAddLevelablesLayout().removeChild(this);
            }

            this.onExecute();

            if (this.callback != null && this.useCallback)
                this.callback.run();
        }).horizontalSizing(Sizing.fixed(20));

        return List.of(this.button);
    }

    protected void onExecute() {

    }

    public void addAllExecute() {
        this.useCallback = false;
        this.button.onPress();
        this.useCallback = true;
        if (this.callback != null)
            this.callback.run();
    }
}
