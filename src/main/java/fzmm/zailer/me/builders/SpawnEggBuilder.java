package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

public class SpawnEggBuilder {

    private ItemStack stack;
    private EntityType<?> entityType;
    private NbtCompound entityTag;

    private SpawnEggBuilder() {
        this.stack = Items.BAT_SPAWN_EGG.getDefaultStack();
        this.entityType = EntityType.BAT;
        this.entityTag = new NbtCompound();
    }

    public static SpawnEggBuilder builder() {
        return new SpawnEggBuilder();
    }


    public SpawnEggBuilder item(Item item) {
        this.stack = item.getDefaultStack();
        return this;
    }

    public SpawnEggBuilder entityType(EntityType<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    public SpawnEggBuilder entityTag(NbtCompound entityTag) {
        this.entityTag = entityTag;
        return this;
    }

    public ItemStack get() {
        this.entityTag.putString(TagsConstant.ENTITY_TAG_ID, Registries.ENTITY_TYPE.getId(this.entityType).getPath());
        this.stack.apply(DataComponentTypes.ENTITY_DATA, null,
                entityData -> TypedEntityData.create(this.entityType, this.entityTag)
        );

        return this.stack;
    }
}
