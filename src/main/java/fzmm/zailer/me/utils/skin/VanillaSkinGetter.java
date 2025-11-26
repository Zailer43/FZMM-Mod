package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.response.NameAndId;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Services;
import net.minecraft.world.item.ItemStack;

public class VanillaSkinGetter extends SkinGetterDecorator {

    public VanillaSkinGetter(SkinGetterDecorator skinGetterDecorator) {
        super(skinGetterDecorator);
    }

    public VanillaSkinGetter() {
        super();
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);
        if (profile.isEmpty()) {
            return super.getSkin(playerName);
        }

        MinecraftProfileTexture skinTexture = Minecraft.getInstance().services().sessionService()
                .getTextures(profile.get())
                .skin();
        if (skinTexture == null) {
            return super.getSkin(playerName);
        }

        try {
            Optional<BufferedImage> result = ImageUtils.getImageFromUrl(skinTexture.getUrl());
            if (result.isPresent()) {
                return result;
            }
        } catch (IOException ignored) {
        }

        return super.getSkin(playerName);
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);
        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    @Override
    public Optional<GameProfile> getProfile(String playerName) {
        Services apiServices = Minecraft.getInstance().services();
        Optional<NameAndId> nameAndId = apiServices.profileRepository().findProfileByName(playerName);
        if (nameAndId.isEmpty()) return super.getProfile(playerName);

        ProfileResult profileResult = apiServices.sessionService().fetchProfile(nameAndId.get().id(), false);
        return profileResult == null ? super.getProfile(playerName) : Optional.of(profileResult.profile());
    }
}
