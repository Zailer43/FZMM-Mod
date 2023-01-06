package fzmm.zailer.me.client.gui.main;

import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.FzmmIcons;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class MainIcon implements Icon {
    private final int width, height;
    private final int u, v;
    private final int textureWidth, textureHeight;
    private static final int WIDTH = 32;
    private static final int HEIGHT = 32;
    public static final MainIcon IMAGETEXT;
    public static final MainIcon TEXT_FORMAT;
    public static final MainIcon PLAYER_STATUE;
    public static final MainIcon ENCRYPTBOOK;
    public static final MainIcon HEAD_GENERATOR;
    public static final MainIcon CONVERTERS;
    public static final MainIcon HISTORY;

    public MainIcon(int width, int height, int u, int v, int textureWidth, int textureHeight) {
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, FzmmIcons.TEXTURE);
        DrawableHelper.drawTexture(matrixStack, x, y, this.width, this.height, this.u, this.v, 16, 16, this.textureWidth, this.textureHeight);
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    static {
        IMAGETEXT = new MainIcon(WIDTH, HEIGHT, 16, 0, 256, 256);
        TEXT_FORMAT = new MainIcon(WIDTH, HEIGHT, 16, 16, 256, 256);
        PLAYER_STATUE = new MainIcon(WIDTH, HEIGHT, 16, 32, 256, 256);
        ENCRYPTBOOK = new MainIcon(WIDTH, HEIGHT, 16, 48, 256, 256);
        HEAD_GENERATOR = new MainIcon(WIDTH, HEIGHT, 16, 64, 256, 256);
        CONVERTERS = new MainIcon(WIDTH, HEIGHT, 16, 80, 256, 256);
        HISTORY = new MainIcon(WIDTH, HEIGHT, 16, 96, 256, 256);
    }
}
