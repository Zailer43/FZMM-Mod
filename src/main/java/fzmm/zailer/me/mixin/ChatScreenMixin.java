package fzmm.zailer.me.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    public void onChatFieldUpdate(String chatText, CallbackInfo ci) {
        this.setFzmmCommandMaxLength(chatText);
    }

    @Redirect(method = "setChatFromHistory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V"))
    protected void setText(TextFieldWidget textField, String text) {
        this.setFzmmCommandMaxLength(text);
        textField.setText(text);
    }

    private void setFzmmCommandMaxLength(String message) {
        if (message.startsWith("/fzmm "))
            this.chatField.setMaxLength(200000);
        else {
            if (this.chatField.getCursor() > 256)
                this.chatField.setCursor(Math.min(256, this.chatField.getText().length()));
            this.chatField.setMaxLength(256);
        }
    }
}
