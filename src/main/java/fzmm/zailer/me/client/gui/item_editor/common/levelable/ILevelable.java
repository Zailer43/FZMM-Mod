package fzmm.zailer.me.client.gui.item_editor.common.levelable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ILevelable<T> {


    /**
     * value can be null in case it is obtained from an item and that ID is not found.
     */
    Optional<T> getValue();

    Identifier valueId();

    Text getName();

    int getLevel();

    void setLevel(int level);

    String getTranslationKey();

    boolean isAcceptableItem(ItemStack stack);

    @Nullable
    Sprite getSprite();

    boolean canHaveSprite();
}
