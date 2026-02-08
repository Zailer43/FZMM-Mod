package fzmm.zailer.me.client.entity.custom_skin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.UUID;

public class CustomPlayerSkinEntity extends RemotePlayer implements ISkinMutable {

    private PlayerSkin textures = new PlayerSkin(DefaultPlayerSkin.getDefaultSkin().body(), null, null, PlayerModelType.WIDE, false);
    
    public CustomPlayerSkinEntity(ClientLevel world) {
        super(world, new GameProfile(UUID.randomUUID(), ""));
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, Byte.MAX_VALUE);
    }

    @Override
    public PlayerSkin getSkin() {
        return this.textures;
    }

    @Override
    public PlayerSkin skin() {
        return this.textures;
    }

    @Override
    public void skin(PlayerSkin textures) {
        this.textures = textures;
    }
}
