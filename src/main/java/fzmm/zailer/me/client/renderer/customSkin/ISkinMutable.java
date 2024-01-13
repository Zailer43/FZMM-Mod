package fzmm.zailer.me.client.renderer.customSkin;

import net.minecraft.util.Identifier;

public interface ISkinMutable {

    Identifier getTextures();

    void setSkin(Identifier skin, boolean isSlim);
}
