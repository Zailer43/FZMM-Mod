package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class ChangeColorTab extends AbstractModifyPatternTab {
    @Override
    public String buttonId() {
        return "change-color";
    }

    @Override
    public boolean shouldAddBase() {
        return true;
    }

    @Override
    protected void onItemComponentCreated(HistoryClipboard clipboard, ItemComponent itemComponent,
                                          @Nullable NbtElement pattern, BannerBuilder currentBanner,
                                          DyeColor componentColor) {
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
            this.componentExecute(clipboard, currentBanner, componentColor, pattern);
            return true;
        });

        ItemStack modifiedStack;
        if (isBannerColor) {
            NbtCompound modifiedNbt = itemComponentStack.copy().getNbt();
            Item modifiedItem;
            if (currentBanner.isShield()) {
                modifiedItem = itemComponentStack.getItem();
                if (modifiedNbt != null && modifiedNbt.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
                    modifiedNbt.getCompound(TagsConstant.BLOCK_ENTITY).putInt(ShieldItem.BASE_KEY, componentColor.getId());
                }
            } else {
                modifiedItem = BannerBuilder.getBannerByDye(componentColor);
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
            modifiedPattern.putInt(TagsConstant.BANNER_PATTERN_COLOR, componentColor.getId());
        }

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }

    private void componentExecute(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor selectedColor,
                                  @Nullable NbtElement pattern) {
        UISounds.playButtonSound();

        clipboard.addUndo(currentBanner);

        DyeColor colorCompound = pattern instanceof NbtCompound patternCompound ?
                DyeColor.byId(patternCompound.getInt(TagsConstant.BANNER_PATTERN_COLOR)) : currentBanner.bannerColor();

        if (Screen.hasShiftDown()) {
            currentBanner.replaceColors(selectedColor, colorCompound);
        } else if (pattern == null) {
            currentBanner.bannerColor(selectedColor);
        } else if (pattern instanceof NbtCompound patternCompound) {
            currentBanner.replaceColor(patternCompound, selectedColor);
        }

        clipboard.change(currentBanner);
    }

    @Override
    protected Text getTooltip(@Nullable NbtElement patternElement, BannerBuilder currentBanner, DyeColor selectedColor) {
        Text defaultTooltip = super.getTooltip(patternElement, currentBanner, selectedColor);
        MutableText result = defaultTooltip.copy();

        result.append("\n\n")
                .append(Text.translatable("fzmm.gui.bannerEditor.tab.changeColor.shiftHotkey"));

        return result;
    }
}
