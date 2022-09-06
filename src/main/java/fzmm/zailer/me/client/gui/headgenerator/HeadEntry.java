package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.logic.HeadGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.util.List;

public class HeadEntry extends AbstractHeadListEntry<HeadEntry> {
    private final ButtonWidget giveButton;
    private final ButtonWidget addLayerButton;
    private final HeadListWidget parent;

    public HeadEntry(HeadListWidget parent, MinecraftClient client, String name, BufferedImage previewImage, BufferedImage headTexture) {
        super(client, name, previewImage, headTexture);
        this.parent = parent;
        this.giveButton = new ButtonWidget(0, 0, Buttons.GIVE.getWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.of(Buttons.GIVE.getText()), this::giveButtonExecute);
        this.addLayerButton = new ButtonWidget(0, 0, 20, ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.of(Buttons.ADD.getText()), this::addLayerButtonExecute);
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        int yLine = y + (entryHeight - ScreenConstants.NORMAL_BUTTON_HEIGHT) / 2;

        this.giveButton.x = x + (entryWidth - this.giveButton.getWidth() - 4);
        this.giveButton.y = yLine;
        this.giveButton.render(matrices, mouseX, mouseY, tickDelta);

        this.addLayerButton.x = this.giveButton.x - this.addLayerButton.getWidth() - 4;
        this.addLayerButton.y = yLine;
        this.addLayerButton.render(matrices, mouseX, mouseY, tickDelta);
    }

    private void giveButtonExecute(ButtonWidget button) {
        BufferedImage image = new HeadGenerator(this.getPreviewImage())
                .addTexture(this.getHeadTexture())
                .getHeadTexture();

        this.parent.execute(image);
    }

    private void addLayerButtonExecute(ButtonWidget button) {
        this.parent.addLayer(this);
    }

    public void setEnabled(boolean value) {
        this.giveButton.active = value;
    }

    public ButtonWidget getGiveButton() {
        return this.giveButton;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of(this.addLayerButton, this.giveButton);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(this.addLayerButton, this.giveButton);
    }
}