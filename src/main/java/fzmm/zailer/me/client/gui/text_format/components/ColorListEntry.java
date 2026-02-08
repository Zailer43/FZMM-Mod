package fzmm.zailer.me.client.gui.text_format.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.AbstractRow;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColorListEntry extends ColorRow {
    private static final net.minecraft.network.chat.Component REMOVE_TEXT = net.minecraft.network.chat.Component.translatable("fzmm.gui.button.remove");
    private static final net.minecraft.network.chat.Component UP_ARROW_TEXT = net.minecraft.network.chat.Component.translatable("fzmm.gui.button.arrow.up");
    private static final net.minecraft.network.chat.Component DOWN_ARROW_TEXT = net.minecraft.network.chat.Component.translatable("fzmm.gui.button.arrow.down");
    private final ColorListContainer parent;
    private final ButtonComponent moveUpButton;
    private final ButtonComponent moveDownButton;
    private ButtonComponent removeButton;

    public ColorListEntry(ColorListContainer parent, int id) {
        super(String.valueOf(id), String.valueOf(id), String.valueOf(id));
        this.parent = parent;
        this.moveUpButton = UIComponents.button(UP_ARROW_TEXT, this::upArrowExecute);
        this.moveUpButton.sizing(Sizing.fixed(20), Sizing.fixed(20));

        this.moveDownButton = UIComponents.button(DOWN_ARROW_TEXT, this::downArrowExecute);
        this.moveDownButton.sizing(Sizing.fixed(20), Sizing.fixed(20));

        this.setButtons();
    }

    public void setButtons() {
        Optional<FlowLayout> rowContainerOptional = this.getRowContainer();
        if (rowContainerOptional.isEmpty())
            return;

        String labelId = AbstractRow.getLabelId(this.getId());
        FlowLayout rowContainer = rowContainerOptional.get().gap(BaseFzmmScreen.COMPONENT_DISTANCE);
        List<UIComponent> componentList = new ArrayList<>(List.copyOf(rowContainer.children()));
        componentList.removeIf(component -> labelId.equals(component.id()));

        rowContainer.clearChildren();

        this.removeButton = UIComponents.button(REMOVE_TEXT, buttonComponent -> this.parent.removeColorEntry(this));
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
