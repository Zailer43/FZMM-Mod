package fzmm.zailer.me.mixin.item_group.fix_translations_update;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ItemGroup.DisplayContext.class)
public abstract class ItemGroupDisplayContextMixin {
    @Unique
    private static String fzmm$previousLanguage = null;

    /**
     * As FZMM adds translations to some custom items, and if possible,
     * we want users to be able to see the translation, we use Text#getString()
     * of the translation. This way, we can obtain its value as a String and place it
     * on the item so that it is visible to everyone in the language that the FZMM user
     * was using at that moment, avoiding them seeing a translation key. For this reason,
     * the display group needs to be updated every time the language is changed.
     */
    @ModifyReturnValue(
            method = "doesNotMatch(Lnet/minecraft/resource/featuretoggle/FeatureSet;ZLnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Z",
            at = @At("RETURN")
    )
    public boolean fzmm$updateItemGroupsOnChangeLang(boolean original) {
        String previousLanguage = fzmm$previousLanguage;
        String currentLanguage = MinecraftClient.getInstance().options.language;
        fzmm$previousLanguage = currentLanguage;

        if (previousLanguage != null && !previousLanguage.equals(currentLanguage)) {
            return true;
        }

        return original;
    }
}
