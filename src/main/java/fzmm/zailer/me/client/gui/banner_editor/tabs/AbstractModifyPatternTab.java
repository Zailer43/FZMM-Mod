package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModifyPatternTab implements IBannerTab {

    public abstract boolean shouldAddBase();

    @Override
    public List<Component> update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color) {
        List<Component> bannerList = new ArrayList<>();
        BannerBuilder builder = currentBanner.copy().clearPatterns();

        NbtList patterns = currentBanner.patterns();
        if (this.shouldAddBase()) {
            this.addPreview(clipboard, currentBanner, color, null, builder, bannerList);
        }

        for (var pattern : patterns) {
            builder.addPattern(pattern);
            this.addPreview(clipboard, currentBanner, color, pattern, builder, bannerList);
        }
        return bannerList;
    }

    private void addPreview(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color,
                            @Nullable NbtElement pattern, BannerBuilder builder, List<Component> bannerList) {
        ItemComponent itemComponent = EComponents.item(builder.copy().get());
        itemComponent.sizing(Sizing.fixed(32), Sizing.fixed(32));

        this.onItemComponentCreated(clipboard, itemComponent, pattern, currentBanner, color);
        itemComponent.cursorStyle(CursorStyle.HAND);

        Text tooltip = this.getTooltip(pattern, currentBanner, color);
        itemComponent.tooltip(tooltip);

        bannerList.add(itemComponent);
    }

    protected abstract void onItemComponentCreated(HistoryClipboard clipboard, ItemComponent itemComponent,
                                                   @Nullable NbtElement pattern,
                                                   BannerBuilder currentBanner, DyeColor selectedColor);

    protected Text getTooltip(@Nullable NbtElement patternElement, BannerBuilder currentBanner, DyeColor selectedColor) {
        if (patternElement == null) {
            return Text.translatable("block.minecraft.banner.base." + BannerBuilder.getBannerByDye(selectedColor).toString());
        } else if (patternElement instanceof NbtCompound patternCompound) {
            RegistryEntry<BannerPattern> pattern = BannerPattern.byId(patternCompound.getString(TagsConstant.BANNER_PATTERN_VALUE));
            if (pattern == null) {
                FzmmClient.LOGGER.error("[AbstractModifyPatternTab] pattern is null");
                return Text.empty();
            }
            return BannerBuilder.tooltipOf(selectedColor, pattern);
        } else {
            return Text.empty();
        }
    }
}
