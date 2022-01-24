package fzmm.zailer.me.mixin;

import fzmm.zailer.me.utils.DisplayUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin {

    @Shadow protected abstract ItemStack getAsItemStack();

    @Shadow public abstract void writeCustomDataToNbt(NbtCompound nbt);

    @Inject(method = "getPickBlockStack", at = @At(value = "HEAD"), cancellable = true)
    public void getPickBlockStack(CallbackInfoReturnable<ItemStack> cir) {
        // item_frame{EntityTag:{Item:{id:"minecraft:tnt",Count:1b,tag:{test:1b}},Invisible:1b,Fixed:1b}}

        if (Screen.hasControlDown()) {
            ItemStack stack = this.getAsItemStack();
            NbtCompound entityTag = new NbtCompound();

            this.writeCustomDataToNbt(entityTag);

            entityTag.remove("TileX");
            entityTag.remove("TileY");
            entityTag.remove("TileZ");
            entityTag.remove("Facing");

            stack.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
            stack = new DisplayUtils(stack).addLore("(" + EntityType.ENTITY_TAG_KEY + ")").get();
            cir.setReturnValue(stack);
        }
    }
}
