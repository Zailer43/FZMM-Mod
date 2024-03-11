package fzmm.zailer.me.client.gui.item_editor.skull_editor.components;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PlaySoundButtonComponent extends ButtonComponent {
    private static final Text message = Text.translatable("fzmm.gui.itemEditor.skull.button.playSound");
    private SoundEvent sound;

    public PlaySoundButtonComponent() {
        super(message, buttonComponent -> {});
        this.sound = SoundEvents.UI_BUTTON_CLICK.value();
        this.horizontalSizing(Sizing.fixed(20));
    }

    public void setSound(SoundEvent sound) {
        this.sound = sound;
    }

    public void setSound(@Nullable Identifier sound) {
        if (sound == null) {
            this.sound = null;
            return;
        }

        this.sound = SoundEvent.of(sound);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (this.sound != null)
            soundManager.play(PositionedSoundInstance.master(this.sound, 1.0F));
    }
}
