package fzmm.zailer.me.mixin.component.book;

import fzmm.zailer.me.mixin_interfaces.IMultiLineBoxColorFix;
import net.minecraft.client.gui.components.MultiLineEditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiLineEditBox.class)
public abstract class MultiLineEditBoxMixin implements IMultiLineBoxColorFix {

    @Shadow
    @Final
    @Mutable
    private int textColor;

    @Shadow
    @Final
    @Mutable
    private int cursorColor;

    @Override
    public void fzmm$textColor(int value) {
        this.textColor = value;
    }

    @Override
    public void fzmm$cursorColor(int value) {
        this.cursorColor = value;
    }
}
