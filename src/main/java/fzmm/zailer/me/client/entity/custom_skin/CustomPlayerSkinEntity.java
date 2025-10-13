package fzmm.zailer.me.client.entity.custom_skin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;

import java.util.UUID;

public class CustomPlayerSkinEntity extends OtherClientPlayerEntity implements ISkinMutable {

    private SkinTextures textures = new SkinTextures(DefaultSkinHelper.getSteve().body(), null, null, PlayerSkinType.WIDE, false);
    
    public CustomPlayerSkinEntity(ClientWorld world) {
        super(world, new GameProfile(UUID.randomUUID(), ""));
        this.getDataTracker().set(PLAYER_MODE_CUSTOMIZATION_ID, Byte.MAX_VALUE);
    }

    @Override
    public SkinTextures getSkin() {
        return this.textures;
    }

    @Override
    public SkinTextures skin() {
        return this.textures;
    }

    @Override
    public void skin(SkinTextures textures) {
        this.textures = textures;
    }
}
