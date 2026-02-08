package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class CacheSkinGetter extends SkinGetterDecorator {

    public CacheSkinGetter(SkinGetterDecorator skinGetterDecorator) {
        super(skinGetterDecorator);
    }

    public CacheSkinGetter() {
        super();
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) {
        // obtains the player's skin, if server plugin/mod modifies it, this will obtain that one, which may be convenient
        assert Minecraft.getInstance().getConnection() != null;
        PlayerInfo playerListEntry = Minecraft.getInstance().getConnection().getPlayerInfoIgnoreCase(playerName);
        if (playerListEntry == null) return super.getSkin(playerName);

        Optional<BufferedImage> cacheSkin = this.getSkin(playerListEntry.getSkin());
        return cacheSkin.isPresent() ? cacheSkin : super.getSkin(playerName);
    }

    public Optional<BufferedImage> getSkin(PlayerSkin textures) {
        AbstractTexture texture;
        try {
            texture = Minecraft.getInstance().getTextureManager().getTexture(textures.body().texturePath());
        } catch (Exception e) {
            texture = null;
        }
        if (!(texture instanceof DynamicTexture nativeTexture)) return Optional.empty();

        NativeImage nativeImage = nativeTexture.getPixels();
        if (nativeImage == null) return Optional.empty();

        return Optional.of(ImageUtils.getBufferedImgFromNativeImg(nativeImage));
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);

        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    @Override
    protected Optional<GameProfile> getProfile(String playerName) {
        assert Minecraft.getInstance().getConnection() != null;
        PlayerInfo playerListEntry = Minecraft.getInstance().getConnection().getPlayerInfoIgnoreCase(playerName);

        return Optional.ofNullable(playerListEntry == null ? null : playerListEntry.getProfile());
    }
}
