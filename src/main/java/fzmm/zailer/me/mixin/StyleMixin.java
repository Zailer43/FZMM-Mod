package fzmm.zailer.me.mixin;

import fzmm.zailer.me.config.FzmmConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public class StyleMixin {

    @Inject(method = "isObfuscated", at = @At("HEAD"), cancellable = true)
    public void isObfuscated(CallbackInfoReturnable<Boolean> cir) {
        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        if (config.general.textObfuscated) cir.setReturnValue(false);
    }
}
