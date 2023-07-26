package fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;

public class ChangeColorTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_LAYOUT = "change-color-layout";
    @Override
    public String getId() {
        return "changeColor";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_LAYOUT;
    }

    @Override
    public boolean shouldAddBaseColor() {
        return true;
    }

    @Override
    protected void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color) {
        ItemStack itemComponentStack = itemComponent.stack();

        NbtList patterns = currentBanner.patterns();
        int index = -1;
        int patternsSize = patterns.size();

        for (int i = 0; i != patternsSize; i++) {
            if (patterns.get(i) == pattern) {
                index = i;
                break;
            }
        }

        boolean isBannerColor = index == -1;

        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playButtonSound();

            if (isBannerColor) {
                currentBanner.bannerColor(color);
            } else if (pattern instanceof NbtCompound patternCompound) {
                patternCompound.putInt(TagsConstant.BANNER_PATTERN_COLOR, color.getId());
            }

            parent.updatePreview(currentBanner);
            return true;
        });

        ItemStack modifiedStack;
        if (isBannerColor) {
            NbtCompound modifiedNbt = itemComponentStack.copy().getNbt();
            Item modifiedItem;
            if (currentBanner.isShield()) {
                modifiedItem = itemComponentStack.getItem();
                if (modifiedNbt != null && modifiedNbt.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
                    modifiedNbt.getCompound(TagsConstant.BLOCK_ENTITY).putInt(ShieldItem.BASE_KEY, color.getId());
                }
            } else {
                modifiedItem = BannerBuilder.getBannerByDye(color);
            }
            modifiedStack = modifiedItem.getDefaultStack();
            modifiedStack.setNbt(modifiedNbt);
        } else {
            modifiedStack = itemComponentStack.copy();
            NbtCompound blockEntityTag = modifiedStack.getSubNbt(TagsConstant.BLOCK_ENTITY);
            if (blockEntityTag == null)
                return;
            NbtList modifiedPatterns = blockEntityTag.getList(TagsConstant.BANNER_PATTERN, NbtElement.COMPOUND_TYPE);
            if (modifiedPatterns == null)
                return;
            NbtCompound modifiedPattern = (NbtCompound) modifiedPatterns.get(index);
            modifiedPattern.putInt(TagsConstant.BANNER_PATTERN_COLOR, color.getId());
        }

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }
}
