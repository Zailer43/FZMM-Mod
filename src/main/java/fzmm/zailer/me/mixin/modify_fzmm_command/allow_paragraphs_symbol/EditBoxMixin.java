package fzmm.zailer.me.mixin.modify_fzmm_command.allow_paragraphs_symbol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fzmm.zailer.me.mixin_interfaces.IAllowParagraphs;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public abstract class EditBoxMixin implements IAllowParagraphs {

    @Shadow public abstract void setValue(String text);

    @Shadow public abstract String getValue();

    @Shadow public abstract int getCursorPosition();

    @Shadow public abstract void moveCursorTo(int cursor, boolean shiftKeyPressed);

    @Unique
    private boolean fzmm$allowParagraphs = false;

    @WrapOperation(
            method = "insertText(Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtil;filterText(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String fzmm$allowParagraphInWrite(String string, Operation<String> original) {
        if (this.fzmm$allowParagraphs) {
            return string;
        }

        return original.call(string);
    }

    @WrapOperation(
            method = "charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/CharacterEvent;isAllowedChatCharacter()Z")
    )
    private boolean fzmm$allowParagraphInCharTyped(CharacterEvent instance, Operation<Boolean> original) {
        if (this.fzmm$allowParagraphs) return true;

        return original.call(instance);
    }

    @Unique
    @Override
    public void fzmm$setAllowParagraphs(boolean allowParagraphs) {
        this.fzmm$allowParagraphs = allowParagraphs;

        if (!allowParagraphs) {
            String text = this.getValue();
            String strippedText = StringUtil.filterText(text);

            if (!strippedText.equals(text)) {
                int cursorPosition = this.getCursorPosition();
                this.setValue(strippedText);
                this.moveCursorTo(Mth.clamp(cursorPosition, 0, strippedText.length()), false);
            }
        }
    }
}
