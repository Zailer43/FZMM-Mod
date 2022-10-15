package fzmm.zailer.me.client;

import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.util.Identifier;

public class FzmmIcons {
    public static final Identifier TEXTURE = new Identifier(FzmmClient.MOD_ID, "textures/gui/widgets.png");
    public static final Icon ERROR;
    public static final Icon SUCCESSFUL;
    public static final Icon LOADING;

    static {
        ERROR = Icon.of(TEXTURE, 0, 0, 256, 256);
        SUCCESSFUL = Icon.of(TEXTURE, 0, 16, 256, 256);
        LOADING = Icon.of(TEXTURE, 0, 32, 256, 256);
    }
}
