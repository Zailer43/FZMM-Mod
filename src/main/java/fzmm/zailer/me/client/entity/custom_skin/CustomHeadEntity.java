package fzmm.zailer.me.client.entity.custom_skin;

import fzmm.zailer.me.client.FzmmClient;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

public class CustomHeadEntity extends MobEntity implements ISkinMutable {
    public static final EntityType<CustomHeadEntity> CUSTOM_HEAD_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            FzmmClient.CUSTOM_HEAD_ENTITY,
            FabricEntityTypeBuilder.<CustomHeadEntity>create(SpawnGroup.MISC)
                    .disableSaving()
                    .disableSummon()
                    .dimensions(EntityDimensions.fixed(0.8f, 0.8f))
                    .trackRangeBlocks(32)
                    .trackedUpdateRate(2)
                    .entityFactory((type, world) -> new CustomHeadEntity(world))
                    .build()
    );

    private SkinTextures textures = new SkinTextures(DefaultSkinHelper.getTexture(), null, null, null, SkinTextures.Model.WIDE, false);

    public CustomHeadEntity(World world) {
        super(CUSTOM_HEAD_ENTITY_TYPE, world);
    }

    @Override
    public SkinTextures skin() {
        return this.textures;
    }

    public void skin(SkinTextures textures) {
        this.textures = textures;
    }
}