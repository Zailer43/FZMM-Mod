package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;

public interface ISkinMutable {

    SkinTextures skin();

    void skin(SkinTextures textures);

    default void skin(AssetInfo.TextureAsset body, PlayerSkinType type) {
        skin(new SkinTextures(body,
                null,
                null,
                type,
                false
        ));
    }

    default void texture(AssetInfo.TextureAsset body) {
        this.skin(body, this.skin().model());
    }

    default void model(boolean isSlim) {
        this.skin(this.skin().body(), isSlim ? PlayerSkinType.SLIM : PlayerSkinType.WIDE);
    }
}
