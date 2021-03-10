package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.ReplaceText;
import fzmm.zailer.me.client.ToggleFont;
import fzmm.zailer.me.config.FzmmConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class PlayerMixin {

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String msg, CallbackInfo info) {
        info.cancel();

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        if (config.replaceTexts.enableReplaceText) msg = ReplaceText.replace(msg);

        if (config.general.toggleFont && msg.charAt(0) != '/') msg = ToggleFont.convert(msg);

        networkHandler.sendPacket(new ChatMessageC2SPacket(msg));

    }
}