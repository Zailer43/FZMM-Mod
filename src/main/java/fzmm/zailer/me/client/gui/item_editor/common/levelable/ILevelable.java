package fzmm.zailer.me.client.gui.item_editor.common.levelable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ILevelable<T> {


    T getValue();

    Text getName();

    int getLevel();

    void setLevel(int level);

    String getTranslationKey();

    boolean isAcceptableItem(ItemStack stack);

    @Nullable
    Sprite getSprite();
}
