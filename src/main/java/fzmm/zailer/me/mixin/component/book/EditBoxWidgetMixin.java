package fzmm.zailer.me.mixin.component.book;

import fzmm.zailer.me.mixin_interfaces.IEditBoxTextColorFix;
import net.minecraft.client.gui.widget.EditBoxWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EditBoxWidget.class)
public abstract class EditBoxWidgetMixin implements IEditBoxTextColorFix {

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
