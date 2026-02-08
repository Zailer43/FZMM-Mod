package fzmm.zailer.me.client.entity.custom_skin;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;

public class CustomHeadEntity extends Mob implements ISkinMutable {
    public static final EntityType<CustomHeadEntity> CUSTOM_HEAD_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            FzmmClient.CUSTOM_HEAD_ENTITY,
            EntityType.Builder.<CustomHeadEntity>of((type, world) -> new CustomHeadEntity(world), MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(32)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, FzmmClient.CUSTOM_HEAD_ENTITY))
    );

    private PlayerSkin textures = DefaultPlayerSkin.getDefaultSkin();

    public CustomHeadEntity(Level world) {
        super(CUSTOM_HEAD_ENTITY_TYPE, world);
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