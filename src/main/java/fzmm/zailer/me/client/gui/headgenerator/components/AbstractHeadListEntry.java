package fzmm.zailer.me.client.gui.headgenerator.components;

import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.client.logic.headGenerator.HeadGeneratorResources;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.Objects;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public abstract class AbstractHeadListEntry extends HorizontalFlowLayout {
    private static final int PLAYER_SKIN_SIZE = 24;
    private Identifier previewIdentifier;
    protected HeadData headData;

    public AbstractHeadListEntry(HeadData headData) {
        super(Sizing.fill(100), Sizing.fixed(28));
        this.headData = headData;
        this.updatePreview();
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int xWithPadding = this.x + 4;
        int centerY = this.y + (this.height / 2);
        int xText = xWithPadding + 24 + 4;
        Objects.requireNonNull(textRenderer);
        int yText = centerY - textRenderer.fontHeight / 2;

        RenderSystem.setShaderTexture(0, this.previewIdentifier);
        PlayerSkinDrawer.draw(matrices, xWithPadding, centerY - PLAYER_SKIN_SIZE / 2, PLAYER_SKIN_SIZE);

        textRenderer.draw(matrices, this.getName(), (float) xText, (float) yText, WHITE_COLOR);
    }
    public String getName() {
        return this.headData.name();
    }

    public abstract void setEnabled(boolean value);

    public BufferedImage getPreviewImage() {
        return this.headData.skin();
    }

    public BufferedImage getHeadTextureByName() {
        return HeadGeneratorResources.getTexture(this.getName());
    }

    public void update(BufferedImage skinBase) {
        BufferedImage head = HeadGeneratorResources.getTexture(this.headData.name());
        if (head == null)
            head = skinBase;

        this.headData = new HeadData(new HeadGenerator(skinBase).addTexture(head).getHeadTexture(), this.headData.name());
        this.updatePreview();
    }

    private void updatePreview() {
        this.previewIdentifier = FzmmUtils.saveBufferedImageAsIdentifier(headData.skin());
    }
}