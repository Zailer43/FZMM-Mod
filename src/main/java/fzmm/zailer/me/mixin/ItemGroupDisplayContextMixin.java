package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.DisplayContext.class)
public abstract class ItemGroupDisplayContextMixin {
    private String previousLanguage = null;

    @Inject(method = "doesNotMatch(Lnet/minecraft/resource/featuretoggle/FeatureSet;ZLnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    public void updateItemGroupsOnChangeLang(FeatureSet enabledFeatures, boolean hasPermissions, RegistryWrapper.WrapperLookup lookup, CallbackInfoReturnable<Boolean> cir) {
        String previousLanguage = this.previousLanguage;
        String currentLanguage = MinecraftClient.getInstance().options.language;
        this.previousLanguage = currentLanguage;

        if (previousLanguage != null && !previousLanguage.equals(currentLanguage)) {
            cir.setReturnValue(true);
        }
    }
}
