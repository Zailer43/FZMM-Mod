package fzmm.zailer.me.client.gui.headgenerator;

import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGeneratorResources;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.Objects;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public abstract class AbstractHeadListEntry extends BaseComponent {
    private final Identifier previewIdentifier;
    private final HeadData headData;

    public AbstractHeadListEntry(HeadData headData, Sizing horizontalSizing, Sizing verticalSizing) {
        this.previewIdentifier = FzmmUtils.saveBufferedImageAsIdentifier(headData.skin());
        this.headData = headData;
        this.sizing(horizontalSizing, verticalSizing);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int xWithPadding = this.x + 4;
        int centerY = this.y + (this.height / 2);
        int xText = xWithPadding + 24 + 4;
        Objects.requireNonNull(textRenderer);
        int yText = centerY - textRenderer.fontHeight / 2;

        RenderSystem.setShaderTexture(0, this.previewIdentifier);
        PlayerSkinDrawer.draw(matrices, xWithPadding, this.y, 24);

        textRenderer.draw(matrices, this.getName(), (float) xText, (float) yText, WHITE_COLOR);
    }
    public String getName() {
        return this.headData.name();
    }

    public abstract void setEnabled(boolean value);

    public BufferedImage getPreviewImage() {
        return this.headData.skin();
    }

    public BufferedImage getHeadTexture() {
        return HeadGeneratorResources.getTexture(this.getName());
    }
}