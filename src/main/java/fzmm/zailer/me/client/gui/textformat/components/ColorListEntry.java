package fzmm.zailer.me.client.gui.textformat.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColorListEntry extends ColorRow {
    private static final Text REMOVE_TEXT = Text.translatable("fzmm.gui.button.remove");
    private static final Text UP_ARROW_TEXT = Text.translatable("fzmm.gui.button.arrow.up");
    private static final Text DOWN_ARROW_TEXT = Text.translatable("fzmm.gui.button.arrow.down");
    private final ColorListContainer parent;
    private final ButtonComponent moveUpButton;
    private final ButtonComponent moveDownButton;
    private ButtonComponent removeButton;

    public ColorListEntry(ColorListContainer parent, int id) {
        super(String.valueOf(id), String.valueOf(id), String.valueOf(id));
        this.parent = parent;
        this.moveUpButton = Components.button(UP_ARROW_TEXT, this::upArrowExecute);
        this.moveUpButton.sizing(Sizing.fixed(20), Sizing.fixed(20));

        this.moveDownButton = Components.button(DOWN_ARROW_TEXT, this::downArrowExecute);
        this.moveDownButton.sizing(Sizing.fixed(20), Sizing.fixed(20));

        this.setButtons();
    }

    public void setButtons() {
        Optional<FlowLayout> rowContainerOptional = this.getRowContainer();
        if (rowContainerOptional.isEmpty())
            return;

        String labelId = AbstractRow.getLabelId(this.getId());
        FlowLayout rowContainer = rowContainerOptional.get().gap(BaseFzmmScreen.COMPONENT_DISTANCE);
        List<Component> componentList = new ArrayList<>(List.copyOf(rowContainer.children()));
        componentList.removeIf(component -> labelId.equals(component.id()));

        rowContainer.clearChildren();

        this.removeButton = Components.button(REMOVE_TEXT, buttonComponent -> this.parent.removeColorEntry(this));
        this.removeButton.sizing(Sizing.fixed(20), Sizing.fixed(20));
        this.removeButton.margins(Insets.left(15));
        rowContainer.child(this.removeButton);
        rowContainer.child(this.moveUpButton);
        rowContainer.child(this.moveDownButton);

        rowContainer.children(componentList);
    }

    private void upArrowExecute(ButtonComponent buttonComponent) {
        this.parent.upEntry(this);
    }

    private void downArrowExecute(ButtonComponent buttonComponent) {
        this.parent.downEntry(this);
    }

    public void setRemoveButtonActive(boolean value) {
        this.removeButton.active = value;
    }

    public void setMoveUpButtonActive(boolean value) {
        this.moveUpButton.active = value;
    }

    public void setMoveDownButtonActive(boolean value) {
        this.moveDownButton.active = value;
    }
}
