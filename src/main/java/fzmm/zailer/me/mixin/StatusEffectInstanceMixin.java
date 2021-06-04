package fzmm.zailer.me.mixin;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {

    @Inject(method = "updateDuration", at = @At("HEAD"))
    private void updateDuration(CallbackInfoReturnable<Integer> cir) {

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
        if (config.general.disableNightVisionIfBlindness) {
            MinecraftClient mc = MinecraftClient.getInstance();
            assert mc.player != null;

            if (mc.player.getStatusEffects().toString().contains("night_vision")
                && mc.player.getStatusEffects().toString().contains("blindness")) {

                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }
}
