package fzmm.zailer.me.client.gui.bannereditor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.bannereditor.BannerEditorScreen;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.ui.component.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;

public class ChangeColorTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_GRID = "change-color-grid";
    @Override
    public String getId() {
        return "changeColor";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_GRID;
    }

    @Override
    protected void onItemCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color) {
        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (pattern instanceof NbtCompound patternCompound) {
                patternCompound.putInt(TagsConstant.BANNER_PATTERN_COLOR, color.getId());
            }

            parent.updatePreview(currentBanner);
            return true;
        });

        ItemStack itemComponentStack = itemComponent.stack();
        ItemStack modifiedStack = itemComponent.stack().copy();

        NbtList patterns = currentBanner.patterns();
        int index = 0;
        int patternsSize = patterns.size();

        for (int i = 0; i != patternsSize; i++) {
            if (patterns.get(i) == pattern) {
                index = i;
                break;
            }
        }

        NbtCompound blockEntityTag = modifiedStack.getSubNbt(TagsConstant.BLOCK_ENTITY);
        if (blockEntityTag == null)
            return;
        NbtList modifiedPatterns = blockEntityTag.getList(TagsConstant.BANNER_PATTERN, NbtElement.COMPOUND_TYPE);
        if (modifiedPatterns == null)
            return;
        NbtCompound modifiedPattern = (NbtCompound) modifiedPatterns.get(index);
        modifiedPattern.putInt(TagsConstant.BANNER_PATTERN_COLOR, color.getId());

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }
}
