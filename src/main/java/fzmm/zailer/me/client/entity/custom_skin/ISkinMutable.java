package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

public interface ISkinMutable {

    SkinTextures skin();

    void skin(SkinTextures skin);

    default void skin(Identifier skin, SkinTextures.Model model) {
        this.skin(new SkinTextures(skin,
                null,
                null,
                null,
                model,
                false
        ));
    }

    default void texture(Identifier skin) {
        this.skin(skin, this.skin().model());
    }

    default void model(boolean isSlim) {
        this.skin(this.skin().texture(), isSlim ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE);
    }
}
