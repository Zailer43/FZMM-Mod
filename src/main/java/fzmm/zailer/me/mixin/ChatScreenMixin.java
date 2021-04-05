package fzmm.zailer.me.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    public void onChatFieldUpdate(String chatText, CallbackInfo ci) {

        if (chatText.startsWith("/fzmm ")) this.chatField.setMaxLength(30000);
        else this.chatField.setMaxLength(256);
    }
}
