package fzmm.zailer.me.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow public abstract Text getName();

	@Inject(method = "getPickBlockStack", at = @At("HEAD"), cancellable = true)
	public void getPickBlockStack(CallbackInfoReturnable<ItemStack> cir) {
		if ((Object) this instanceof PlayerEntity) {
			ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
			NbtCompound skullOwner = new NbtCompound();

			skullOwner.putString("SkullOwner", this.getName().getString());

			head.setTag(skullOwner);
			cir.setReturnValue(head);
		}
	}
}
