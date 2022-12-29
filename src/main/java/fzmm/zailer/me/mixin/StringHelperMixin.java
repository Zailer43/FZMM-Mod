package fzmm.zailer.me.mixin;

import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(StringHelper.class)
public abstract class StringHelperMixin {

    @ModifyArgs(method = "truncateChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;truncate(Ljava/lang/String;IZ)Ljava/lang/String;"))
    private static void fixTruncateChatWithFzmmCommand(Args args) {
        String text = args.get(0);
        if (text.startsWith("/fzmm "))
            args.set(1, 200000);
    }
}
