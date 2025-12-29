package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;

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
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getCaseInsensitivePlayerInfo(playerName);
        if (playerListEntry == null) return super.getSkin(playerName);

        Optional<BufferedImage> cacheSkin = this.getSkin(playerListEntry.getSkinTextures());
        return cacheSkin.isPresent() ? cacheSkin : super.getSkin(playerName);
    }

    public Optional<BufferedImage> getSkin(SkinTextures textures) {
        AbstractTexture texture;
        try {
            texture = MinecraftClient.getInstance().getTextureManager().getTexture(textures.body().texturePath());
        } catch (Exception e) {
            texture = null;
        }
        if (!(texture instanceof NativeImageBackedTexture nativeTexture)) return Optional.empty();

        NativeImage nativeImage = nativeTexture.getImage();
        if (nativeImage == null) return Optional.empty();

        return Optional.of(ImageUtils.getBufferedImgFromNativeImg(nativeImage));
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);

        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    @Override
    public Optional<GameProfile> getProfile(String playerName) {
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        PlayerListEntry playerListEntry = MinecraftClient.getInstance().getNetworkHandler().getCaseInsensitivePlayerInfo(playerName);

        return playerListEntry == null ? super.getProfile(playerName) : Optional.ofNullable(playerListEntry.getProfile());
    }
}
