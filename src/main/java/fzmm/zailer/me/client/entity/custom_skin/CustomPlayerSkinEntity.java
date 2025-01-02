package fzmm.zailer.me.client.entity.custom_skin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;

import java.util.UUID;

public class CustomPlayerSkinEntity extends OtherClientPlayerEntity implements ISkinMutable {

    private SkinTextures textures = new SkinTextures(DefaultSkinHelper.getTexture(), null, null, null, SkinTextures.Model.WIDE, false);
    
    public CustomPlayerSkinEntity(ClientWorld world) {
        super(world, new GameProfile(UUID.randomUUID(), ""));
        this.getDataTracker().set(PLAYER_MODEL_PARTS, Byte.MAX_VALUE);
    }

    @Override
    public SkinTextures getSkinTextures() {
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
