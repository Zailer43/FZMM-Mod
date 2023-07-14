package fzmm.zailer.me.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    private void fzmm$onChatFieldUpdate(String chatText, CallbackInfo ci) {
        this.fzmm$setFzmmCommandMaxLength(chatText);
    }

    @Redirect(method = "setChatFromHistory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V"))
    private void fzmm$setText(TextFieldWidget textField, String text) {
        this.fzmm$setFzmmCommandMaxLength(text);
        textField.setText(text);
    }

    private void fzmm$setFzmmCommandMaxLength(String message) {
        if (message.startsWith("/fzmm "))
            this.chatField.setMaxLength(200000);
        else {
            if (this.chatField.getCursor() > 256)
                this.chatField.setCursor(Math.min(256, this.chatField.getText().length()));
            this.chatField.setMaxLength(256);
        }
    }

    @Inject(method = "normalize", at = @At(value = "HEAD"), cancellable = true)
    private void fzmm$avoidNormalizeWithFzmmCommand(String str, CallbackInfoReturnable<String> cir) {
        if (str.startsWith("/fzmm "))
            cir.setReturnValue(str);
    }
}
