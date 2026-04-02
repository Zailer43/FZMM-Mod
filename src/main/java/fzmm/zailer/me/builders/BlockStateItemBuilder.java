package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmItemGroup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class BlockStateItemBuilder {

    private final Item item;
    @Nullable
    private final String itemName;
    private final BlockItemStateProperties blockStateComponent = new BlockItemStateProperties(new HashMap<>());

    public BlockStateItemBuilder(Item item, String itemNameTranslationKey) {
        this.item = item;
        this.itemName = Component.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".item." + itemNameTranslationKey).getString();
    }

    public BlockStateItemBuilder(Item item, String translationKey, Item translationItem) {
        this.item = item;
        this.itemName = Component.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".item." + translationKey, translationItem.getDefaultInstance().getDisplayName().getString()).getString();
    }

    public ItemStack get() {
        DisplayBuilder displayBuilder = DisplayBuilder.builder().stack(this.item.getDefaultInstance());
        if (this.itemName != null) {
            int color = FzmmClient.CONFIG.colors.usefulBlockStates().rgb();

            displayBuilder.setName(this.itemName, color)
                    .addLore(Component.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".place").getString(), color);
        }
        ItemStack stack = displayBuilder.get();

        stack.update(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY, component -> blockStateComponent);
        return stack;
    }

    public BlockStateItemBuilder add(String key, String value) {
        this.blockStateComponent.properties().put(key, value);
        return this;
    }

    public BlockStateItemBuilder add(String key, boolean value) {
        return this.add(key, String.valueOf(value));
    }

    public BlockStateItemBuilder add(String key, int value) {
        return this.add(key, String.valueOf(value));
    }
}
