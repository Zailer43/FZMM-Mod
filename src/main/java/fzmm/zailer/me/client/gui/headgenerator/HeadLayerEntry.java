package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.util.List;

public class HeadLayerEntry extends AbstractHeadListEntry<HeadLayerEntry> {
    private final HeadLayersListWidget parent;
    private final ButtonWidget removeButton;

    public HeadLayerEntry(HeadLayersListWidget parent, MinecraftClient client, String name, BufferedImage previewImage, BufferedImage headTexture) {
        super(client, name, previewImage, headTexture);
        this.parent = parent;
        this.removeButton = new ButtonWidget(0, 0, 20, ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.of(Buttons.REMOVE.getText()), this::removeButtonExecute);
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);

        this.removeButton.x = x + (entryWidth - this.removeButton.getWidth() - 4);
        this.removeButton.y = y + (entryHeight - this.removeButton.getHeight()) / 2;
        this.removeButton.render(matrices, mouseX, mouseY, tickDelta);
    }

    public void setEnabled(boolean value) {
        this.removeButton.active = value;
    }

    public void removeButtonExecute(ButtonWidget button) {
        this.parent.remove(this);
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.removeButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.removeButton);
    }
}