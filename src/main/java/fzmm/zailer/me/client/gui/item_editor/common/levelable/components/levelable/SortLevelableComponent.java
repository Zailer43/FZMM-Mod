package fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

import java.util.List;

public class SortLevelableComponent<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> extends BaseLevelableComponent<V, D, B>
        implements IListEntry<SortLevelableComponent<V, D, B>> {

    private ButtonComponent upButton;
    private ButtonComponent downButton;
    private LabelComponent levelLabel;

    public SortLevelableComponent(D levelable, LevelableEditor<V, D, B> editor, B builder) {
        super(levelable, editor, builder);
    }

    @Override
    protected List<? extends Component> getOptions() {
        String level = String.valueOf(this.getLevelable().getLevel());
        this.levelLabel = Components.label(Text.literal(level));
        return List.of(this.levelLabel);
    }

    @Override
    protected List<ButtonComponent> getButtons(LevelableEditor<V, D, B> editor, B builder) {
        this.upButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.up"), button -> {})
                .horizontalSizing(Sizing.fixed(20));

        this.downButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.down"), button -> {})
                .horizontalSizing(Sizing.fixed(20));

        return List.of(this.upButton, this.downButton);
    }

    public ButtonComponent getUpButton() {
        return this.upButton;
    }

    public ButtonComponent getDownButton() {
        return this.downButton;
    }

    @Override
    public SortLevelableComponent<V, D, B> getValue() {
        return new SortLevelableComponent<>(this.getLevelable(), this.editor, this.builder);
    }

    @Override
    public void setValue(SortLevelableComponent<V, D, B> value) {
        this.setDisabled(value.isDisabled);
        this.setButtonList(value.buttonList);
        this.setLevelable(value.getLevelable());
        this.levelLabel.text(value.levelLabel.text());

        if (this.spriteLayout != null)
            this.spriteLayout.clearChildren();
        this.getSpriteComponent().ifPresent(spriteComponent -> this.spriteLayout.child(spriteComponent));
    }
}
