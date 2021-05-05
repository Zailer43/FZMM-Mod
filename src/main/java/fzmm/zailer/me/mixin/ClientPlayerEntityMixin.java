package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.ReplaceText;
import fzmm.zailer.me.client.ToggleFont;
import fzmm.zailer.me.client.commands.StartWith;
import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
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
public abstract class ClientPlayerEntityMixin {

    @Shadow @Final public ClientPlayNetworkHandler networkHandler;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String msg, CallbackInfo ci) {
        ci.cancel();

        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        if (config.replaceTexts.enableReplaceText) msg = ReplaceText.replace(msg);

        if (config.general.toggleFont && msg.charAt(0) != '/') msg = ToggleFont.convert(msg);

        if (msg.startsWith("/")) networkHandler.sendPacket(new ChatMessageC2SPacket(msg));
        else networkHandler.sendPacket(new ChatMessageC2SPacket(StartWith.startWithMsg + msg));

    }
}