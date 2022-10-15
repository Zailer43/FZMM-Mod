package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public class HeadComponentEntry extends AbstractHeadListEntry {
    private final ButtonWidget giveButton;
    private final ButtonWidget addLayerButton;

    public HeadComponentEntry(HeadData headData, Sizing horizontalSizing, Sizing verticalSizing) {
        super(headData, horizontalSizing, verticalSizing);
        Text giveButtonText = Text.translatable("fzmm.gui.button.give");
        this.giveButton = new ButtonWidget(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(giveButtonText), 20, giveButtonText, this::giveButtonExecute);
        this.addLayerButton = new ButtonWidget(0, 0, 20, 20, Text.translatable("fzmm.gui.button.add"), this::addLayerButtonExecute);
    }

//    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//        super.draw(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
//        int yLine = y + (entryHeight - 20) / 2;
//
//        this.giveButton.x = x + (entryWidth - this.giveButton.getWidth() - 4);
//        this.giveButton.y = yLine;
//        this.giveButton.render(matrices, mouseX, mouseY, tickDelta);
//
//        this.addLayerButton.x = this.giveButton.x - this.addLayerButton.getWidth() - 4;
//        this.addLayerButton.y = yLine;
//        this.addLayerButton.render(matrices, mouseX, mouseY, tickDelta);
//    }

    private void giveButtonExecute(ButtonWidget button) {
        BufferedImage image = new HeadGenerator(this.getPreviewImage())
                .addTexture(this.getHeadTexture())
                .getHeadTexture();

//        this.parent.execute(image);
    //todo
    }

    private void addLayerButtonExecute(ButtonWidget button) {
//        this.parent.addLayer(this);
    //todo
    }

    public void setEnabled(boolean value) {
        this.giveButton.active = value;
    }

    protected void applyHorizontalContentSizing(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support horizontal Sizing.content()");
    }

    /**
     * Set the vertical size of this component, based on its content
     */
    protected void applyVerticalContentSizing(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support vertical Sizing.content()");
    }
}