package fzmm.zailer.me.client.gui.headgenerator.components;

import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.Objects;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public abstract class AbstractHeadListEntry extends HorizontalFlowLayout {
    private static final int PLAYER_SKIN_SIZE = 24;
    protected final HeadData headData;
    private Identifier previewIdentifier;

    public AbstractHeadListEntry(HeadData headData) {
        super(Sizing.fill(100), Sizing.fixed(28));
        this.headData = headData;
        this.previewIdentifier = null;
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

        textRenderer.draw(matrices, this.getDisplayName(), (float) xText, (float) yText, WHITE_COLOR);

        if (this.previewIdentifier != null) {
            RenderSystem.setShaderTexture(0, this.previewIdentifier);
            PlayerSkinDrawer.draw(matrices, xWithPadding, centerY - PLAYER_SKIN_SIZE / 2, PLAYER_SKIN_SIZE);
        }
    }

    public String getDisplayName() {
        return this.headData.displayName();
    }

    public BufferedImage getHeadSkin() {
        return this.headData.headSkin();
    }

    public void update(BufferedImage skinBase, boolean overlapHatLayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        client.execute(() -> {
            if (this.previewIdentifier != null)
                textureManager.destroyTexture(this.previewIdentifier);

            BufferedImage previewSkin = new HeadGenerator(skinBase, overlapHatLayer).addTexture(this.headData.headSkin()).getHeadTexture();
            ImageUtils.toNativeImage(previewSkin).ifPresent(nativeImage -> {
                NativeImageBackedTexture preview = new NativeImageBackedTexture(nativeImage);
                this.previewIdentifier = textureManager.registerDynamicTexture("fzmm_head", preview);
            });
            textureManager.bindTexture(this.previewIdentifier);
        });
    }
}