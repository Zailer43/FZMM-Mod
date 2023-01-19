package fzmm.zailer.me.client.gui.headgenerator.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class HeadLayerComponentEntry extends AbstractHeadListEntry {
    private static final Text REMOVE_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.remove");
    private final ButtonComponent removeButton;

    public HeadLayerComponentEntry(HeadData headData, FlowLayout parent) {
        super(headData);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int addLayerButtonWidth = textRenderer.getWidth(REMOVE_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.removeButton = Components.button(REMOVE_LAYER_BUTTON_TEXT, this::removeLayerButtonExecute);
        this.removeButton.sizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)), Sizing.fixed(20))
                .margins(Insets.right(8));

        this.buttonsLayout.child(this.removeButton);

        this.parent = parent;
    }

    private void removeLayerButtonExecute(ButtonComponent button) {
        assert this.parent != null;
        this.parent.removeChild(this);
    }

    public void setEnabled(boolean value) {
        this.removeButton.active = value;
    }
}