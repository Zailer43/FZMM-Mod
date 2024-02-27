package fzmm.zailer.me.client.gui.item_editor.common.levelable;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface ILevelableBuilder<V, DATA extends ILevelable<V>> {

    ItemStack get();

    ILevelableBuilder<V, DATA> add(DATA value);

    ILevelableBuilder<V, DATA> remove(int index);

    /**
     * @return  the same level of the parameter if it does not exceed the maximum possible level (in creative)
     */
    int getMaxLevel(int level);

    boolean isOverMaxLevel();

    ILevelableBuilder<V, DATA> allowDuplicates(boolean value);

    boolean allowDuplicates();

    ILevelableBuilder<V, DATA> clear();

    ILevelableBuilder<V, DATA> stack(ItemStack stack);

    ItemStack stack();

    List<DATA> values();

    ILevelableBuilder<V, DATA> values(List<DATA> values);

    DATA getValue(int index);

    boolean contains(V value);


}
