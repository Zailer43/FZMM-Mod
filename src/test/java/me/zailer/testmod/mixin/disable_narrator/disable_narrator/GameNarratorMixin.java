package me.zailer.testmod.mixin.disable_narrator.disable_narrator;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.text2speech.Narrator;
import net.minecraft.client.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameNarrator.class)
public abstract class GameNarratorMixin {

    @WrapOperation(
            method = "<init>(Lnet/minecraft/client/Minecraft;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;")
    )
    private Narrator fzmmTest$disableNarrator(Operation<Narrator> original) {
        return Narrator.EMPTY; // It's easier to do this than to fix Flatpak permissions :)
    }
}
