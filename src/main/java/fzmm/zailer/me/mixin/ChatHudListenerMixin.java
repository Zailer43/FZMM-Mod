package fzmm.zailer.me.mixin;

import fzmm.zailer.me.config.FzmmConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatHudListenerMixin {

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(MessageType messageType, Text message, UUID senderUuid, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String msg = message.getString();
        FzmmConfig config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();

        for (String regex: config.general.hideMessagesRegex) {
            if (msg.matches(regex)) {
                mc.inGameHud.setOverlayMessage(message, false);
                ci.cancel();
            }
        }
    }
}
