package fzmm.zailer.me.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Shadow @Nullable protected Slot focusedSlot;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == 67 && this.focusedSlot != null && this.focusedSlot.hasStack()) { // 67 = c
            ItemStack stack = this.focusedSlot.getStack();
            MinecraftClient mc = MinecraftClient.getInstance();
            Text name;
            if (stack.hasCustomName()) {
                name = stack.getName();
            } else {
                name = stack.getItem().getName();
            }
            if (Screen.hasControlDown()) {
                mc.keyboard.setClipboard(name.getString());
            } else if (Screen.hasAltDown()) {
                mc.keyboard.setClipboard(Text.Serializer.toJson(name));
            }
        }
    }
}
