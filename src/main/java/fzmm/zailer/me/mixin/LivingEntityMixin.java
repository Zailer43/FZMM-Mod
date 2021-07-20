package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	//TODO: Cambiar esto por redirect en PlayerScreenHandler
	@Inject(method = "getPreferredEquipmentSlot", cancellable = true, at = @At(value = "RETURN"))
	private static void getPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
		assert MinecraftClient.getInstance().player != null;
		if (MinecraftClient.getInstance().player.isCreative() && cir.getReturnValue() == EquipmentSlot.MAINHAND) {
			cir.setReturnValue(EquipmentSlot.HEAD);
		}
	}
}
