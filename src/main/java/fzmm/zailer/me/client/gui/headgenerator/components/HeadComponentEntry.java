package fzmm.zailer.me.client.gui.headgenerator.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public class HeadComponentEntry extends AbstractHeadListEntry {
    public static final Text GIVE_BUTTON_TEXT = Text.translatable("fzmm.gui.headGenerator.button.giveHead");
    private static final Text ADD_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.add");
    private final ButtonComponent giveButton;
    private boolean hide;
    private final Sizing originalVerticalSizing;
    private final HeadGeneratorScreen parentScreen;

    public HeadComponentEntry(HeadData headData, HeadGeneratorScreen parent) {
        super(headData);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int giveButtonWidth = textRenderer.getWidth(GIVE_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.giveButton = Components.button(GIVE_BUTTON_TEXT, this::giveButtonExecute);
        this.giveButton.sizing(Sizing.fixed(giveButtonWidth), Sizing.fixed(20))
                .margins(Insets.right(BaseFzmmScreen.COMPONENT_DISTANCE));

        int addLayerButtonWidth = textRenderer.getWidth(ADD_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent addLayerButton = Components.button(ADD_LAYER_BUTTON_TEXT, this::addLayerButtonExecute);
        addLayerButton.sizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)), Sizing.fixed(20))
                .margins(Insets.right(8));

        FlowLayout buttonsLayout = Containers.horizontalFlow(this.horizontalSizing().get(), this.verticalSizing().get())
                .child(this.giveButton)
                .child(addLayerButton);
        buttonsLayout.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);

        this.child(buttonsLayout);

        this.hide = false;
        this.originalVerticalSizing = this.verticalSizing().get();
        this.parentScreen = parent;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.hide)
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    private void giveButtonExecute(ButtonComponent button) {
        BufferedImage image = new HeadGenerator(this.getPreviewImage())
                .addTexture(this.getHeadTextureByName())
                .getHeadTexture();

        this.parentScreen.giveHead(image);
    }

    private void addLayerButtonExecute(ButtonComponent button) {
        this.parentScreen.addLayer(this.headData);
    }

    @Override
    public void setEnabled(boolean value) {
        this.giveButton.active = value;
    }

    public void filter(String searchValue) {
        if (searchValue.isBlank() || this.getName().toLowerCase().contains(searchValue.toLowerCase())) {
            this.hide = false;
            this.verticalSizing(this.originalVerticalSizing);
        } else {
            this.hide = true;
            this.verticalSizing(Sizing.fixed(0));
        }
    }

    public void updateGiveButton(boolean active, Text text) {
        this.giveButton.active = active;
        this.giveButton.setMessage(text);
    }
}