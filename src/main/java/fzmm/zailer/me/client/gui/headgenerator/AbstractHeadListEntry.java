package fzmm.zailer.me.client.gui.headgenerator;

import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.Objects;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.GRAY_COLOR;
import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public abstract class AbstractHeadListEntry<T extends AbstractHeadListEntry<T>> extends ElementListWidget.Entry<T> {
    private final MinecraftClient client;
    private final String name;
    private final BufferedImage previewImage;
    private final Identifier previewIdentifier;
    private final BufferedImage headTexture;

    public AbstractHeadListEntry(MinecraftClient client, String name, BufferedImage previewImage, BufferedImage headTexture) {
        this.client = client;
        this.name = name;
        this.previewImage = previewImage;
        this.previewIdentifier = FzmmUtils.saveBufferedImageAsIdentifier(this.previewImage);
        this.headTexture = headTexture;
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int xWithPadding = x + 4;
        int lineHeight = y + (entryHeight - 24) / 2;
        int xText = xWithPadding + 24 + 4;
        DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, GRAY_COLOR);
        Objects.requireNonNull(this.client.textRenderer);
        int yText = y + (entryHeight - 9) / 2;

        RenderSystem.setShaderTexture(0, this.previewIdentifier);
        PlayerSkinDrawer.draw(matrices, xWithPadding, lineHeight, 24);

        this.client.textRenderer.draw(matrices, this.name, (float) xText, (float) yText, WHITE_COLOR);
    }

    public String getName() {
        return this.name;
    }

    public abstract void setEnabled(boolean value);

    public BufferedImage getPreviewImage() {
        return this.previewImage;
    }

    public BufferedImage getHeadTexture() {
        return this.headTexture;
    }
}