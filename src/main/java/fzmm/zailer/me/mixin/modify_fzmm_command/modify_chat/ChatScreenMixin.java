package fzmm.zailer.me.mixin.modify_fzmm_command.modify_chat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.mixin_interfaces.IAllowParagraphs;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow
    protected EditBox input;
    @Unique
    protected Integer fzmm$oldMaxLength = null;

    @Inject(method = "onEdited(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void fzmm$onChatFieldUpdate(String chatText, CallbackInfo ci) {
        this.fzmm$tryModifyTextField(chatText);
        // is necessary to update the cursor here because this method is called by TextFieldWidget#onChanged,
        // which causes this method to be called within TextFieldWidget#setMaxLength, so cursor may become invalid
        // before ChatInputSuggestor#refresh, causing a crash
        if (this.fzmm$updateCursor()) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "moveInHistory(I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setValue(Ljava/lang/String;)V")
    )
    private void fzmm$setTextFromHistory(EditBox instance, String text, Operation<Void> original) {
        this.fzmm$tryModifyTextField(text);
        original.call(instance, text);
    }

    @Unique
    private void fzmm$tryModifyTextField(String newText) {
        boolean isFzmmCommand = this.fzmm$isFzmmCommand(newText);
        ((IAllowParagraphs) this.input).fzmm$setAllowParagraphs(isFzmmCommand);
        this.fzmm$setMaxLength(isFzmmCommand);
    }

    @Unique
    private boolean fzmm$updateCursor() {
        int textLength = this.input.getValue().length();
        if (this.input.getCursorPosition() > textLength) {
            this.input.moveCursorTo(textLength, false);
            return true;
        }
        return false;
    }

    @Unique
    private void fzmm$setMaxLength(boolean isFzmmCmd) {
        if (isFzmmCmd) {
            if (this.fzmm$oldMaxLength == null) {
                this.fzmm$oldMaxLength = this.input.getMaxLength();
            }
            this.input.setMaxLength(Integer.MAX_VALUE);
        } else if (this.fzmm$oldMaxLength != null) {
            this.input.setMaxLength(this.fzmm$oldMaxLength);
            this.fzmm$oldMaxLength = null;
        }
    }

    @Unique
    private boolean fzmm$isFzmmCommand(String text) {
        return text.startsWith("/" + FzmmClient.MOD_ID + " ");
    }

    @ModifyReturnValue(method = "normalizeChatMessage(Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "RETURN"))
    private String fzmm$avoidNormalizeWithFzmmCommand(String text) {
        if (this.fzmm$isFzmmCommand(text)) return this.input.getValue().trim();

        return text;
    }
}
