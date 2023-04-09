package fzmm.zailer.me.utils.skin;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.mixin.PlayerSkinTextureAccessor;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GetSkinFromCache extends GetSkinDecorator {

    public GetSkinFromCache(GetSkinDecorator getSkinDecorator) {
        super(getSkinDecorator);
    }

    public GetSkinFromCache() {
        super(null);
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = client.player.networkHandler;
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(playerName);
        if (playerListEntry == null)
            return super.getSkin(playerName);

        Identifier skinIdentifier = playerListEntry.getSkinTexture();
        AbstractTexture texture = client.getTextureManager().getTexture(skinIdentifier);
        // if the player is invisible the texture is not an instance of PlayerSkinTexture
        if (!(texture instanceof PlayerSkinTexture skinTexture))
            return super.getSkin(playerName);

        File skinFile = ((PlayerSkinTextureAccessor) skinTexture).getCacheFile();

        return Optional.of(ImageUtils.getImageFromPath(skinFile.getPath()));
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        assert MinecraftClient.getInstance().player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().player.networkHandler;
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(playerName);

        if (playerListEntry == null)
            return super.getHead(playerName);

        return Optional.of(HeadBuilder.of(playerListEntry.getProfile()));
    }
}
