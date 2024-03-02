package fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SpriteComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class BaseLevelableComponent<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>>  extends FlowLayout {
    private D levelable;
    protected final LabelComponent label;
    protected String labelValue;
    protected List<ButtonComponent> buttonList;
    protected boolean isDisabled;
    protected LevelableEditor<V, D, B> editor;
    protected B builder;
    @Nullable
    protected FlowLayout spriteLayout;

    public BaseLevelableComponent(D levelable, LevelableEditor<V, D, B> editor, B builder) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.editor = editor;
        this.builder = builder;
        this.isDisabled = false;

        this.label = (LabelComponent) Components.label(Text.empty()).horizontalSizing(Sizing.fixed(editor.getLevelablesLabelHorizontalSize()));
        this.setLevelable(levelable);

        this.gap(2);
        this.setupComponent();
    }

    protected void setupComponent() {
        FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));

        this.buttonList = this.getButtons(this.editor, this.builder);
        this.children(this.buttonList);

        layout.children(this.getOptions());

        this.getSpriteComponent().ifPresent(spriteComponent -> {
            this.spriteLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            this.spriteLayout.child(spriteComponent).margins(Insets.of(2));
            layout.child(this.spriteLayout);
        });
        layout.child(this.label);

        layout.gap(2);
        layout.verticalAlignment(VerticalAlignment.CENTER);
        this.child(layout);
    }

    protected Optional<SpriteComponent> getSpriteComponent() {
        Sprite sprite = this.levelable.getSprite();
        if (sprite != null) {
            SpriteComponent spriteComponent = Components.sprite(sprite);
            spriteComponent.sizing(Sizing.fixed(16));
            return Optional.of(spriteComponent);
        }

        return Optional.empty();
    }

    protected abstract List<? extends Component> getOptions();

    protected abstract List<ButtonComponent> getButtons(LevelableEditor<V, D, B> editor, B builder);

    public D getLevelable() {
        return this.levelable;
    }

    public void setLevelable(D levelable) {
        this.levelable = levelable;
        this.setLabel(levelable);
    }

    protected void setLabel(D levelable) {
        this.label.text(this.editor.getLevelableName(levelable));
        this.labelValue = levelable.getName().getString().toLowerCase();
    }

    public boolean filter(String value) {
        return value.isEmpty() || this.labelValue.contains(value.toLowerCase());
    }

    public void setDisabled(boolean value) {
        if (value == this.isDisabled)
            return;

        this.isDisabled = value;
        for (var button : this.buttonList) {
            button.active = !value;
        }

        Style style = this.label.text().getStyle().withStrikethrough(value);
        Text text = this.label.text().copy().setStyle(style);
        this.label.text(text);
    }

    protected void setButtonList(List<ButtonComponent> buttonList) {
        this.buttonList = buttonList;
    }
}
