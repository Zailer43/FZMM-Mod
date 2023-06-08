package fzmm.zailer.me.client.renderer.customSkin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class CustomPlayerSkinEntity extends OtherClientPlayerEntity implements ISkinMutable {

    private static final String SLIM_ID = "slim"; // DefaultSkinHelper.Model.SLIM
    private static final String WIDE_ID = "default"; // DefaultSkinHelper.Model.WIDE

    private Identifier skin;
    private String model;

    public CustomPlayerSkinEntity(ClientWorld world) {
        super(world, new GameProfile(UUID.randomUUID(), null));
        this.getDataTracker().set(PLAYER_MODEL_PARTS, Byte.MAX_VALUE);
    }

    @Override
    public Identifier getSkinTexture() {
        return this.getSkin();
    }

    @Override
    public Identifier getSkin() {
        return this.skin;
    }

    @Override
    public void setSkin(Identifier skin, boolean isSlim) {
        this.skin = skin;
        this.model = isSlim ? SLIM_ID : WIDE_ID;
    }

    @Override
    public String getModel() {
        return this.model;
    }
}
