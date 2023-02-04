package fzmm.zailer.me.client.renderer.customHead;

import fzmm.zailer.me.client.FzmmClient;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CustomHeadEntity extends MobEntity {
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

    private Identifier customHeadTexture;

    public CustomHeadEntity(World world) {
        super(CUSTOM_HEAD_ENTITY_TYPE, world);
    }

    public Identifier getCustomHeadTexture() {
        return this.customHeadTexture;
    }

    public void setCustomHeadTexture(Identifier customHeadTexture) {
        this.customHeadTexture = customHeadTexture;
    }
}