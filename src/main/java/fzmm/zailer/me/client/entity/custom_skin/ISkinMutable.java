package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public interface ISkinMutable {

    PlayerSkin skin();

    void skin(PlayerSkin textures);

    default void skin(ClientAsset.Texture body, PlayerModelType type) {
        skin(new PlayerSkin(body,
                null,
                null,
                type,
                false
        ));
    }

    default void texture(ClientAsset.Texture body) {
        this.skin(body, this.skin().model());
    }

    default void model(boolean isSlim) {
        this.skin(this.skin().body(), isSlim ? PlayerModelType.SLIM : PlayerModelType.WIDE);
    }
}
