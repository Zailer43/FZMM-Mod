package fzmm.zailer.me.client.gui.enums;

import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import fi.dy.masa.malilib.render.RenderUtils;
import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.util.Identifier;

public enum FzmmIcons implements IGuiIcon {
    ERROR(0, 0, 16, 16),
    SUCCESSFUL(0, 16, 16, 16),
    LOADING(0, 32, 16, 16);

    public static final Identifier TEXTURE = new Identifier(FzmmClient.MOD_ID, "textures/gui/widgets.png");
    private final int u;
    private final int v;
    private final int width;
    private final int height;

    FzmmIcons(int u, int v, int width, int height) {
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getU() {
        return this.u;
    }

    @Override
    public int getV() {
        return this.v;
    }

    @Override
    public void renderAt(int x, int y, float zLevel, boolean enabled, boolean selected) {
        RenderUtils.bindTexture(TEXTURE);
        RenderUtils.drawTexturedRect(x, y, this.u, this.v, this.width, this.height, zLevel);
    }

    public void renderAt(int x, int y, float zLevel) {
        this.renderAt(x, y, zLevel, false, false);
    }

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }
}
