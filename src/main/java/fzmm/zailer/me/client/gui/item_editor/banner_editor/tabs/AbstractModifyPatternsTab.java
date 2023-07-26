package fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModifyPatternsTab implements IBannerEditorTab {

    protected FlowLayout patternsLayout;

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.patternsLayout = rootComponent.childById(FlowLayout.class, this.getGridId());
        BaseFzmmScreen.checkNull(patternsLayout, "flow-layout", this.getGridId());
    }

    protected abstract String getGridId();

    public abstract boolean shouldAddBaseColor();

    @Override
    public void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color) {
        this.patternsLayout.clearChildren();
        List<Component> bannerList = new ArrayList<>();
        BannerBuilder builder = currentBanner.copy().clearPatterns();

        NbtList patterns = currentBanner.patterns();
        if (this.shouldAddBaseColor()) {
            patterns = currentBanner.copy()
                    .clearPatterns()
                    .addPattern(currentBanner.bannerColor(), BannerPatterns.BASE)
                    .addPatterns(patterns)
                    .patterns();
        }

        for (var pattern : patterns) {
            builder.addPattern(pattern);

            ItemComponent itemComponent = Components.item(builder.copy().get());
            itemComponent.sizing(Sizing.fixed(32), Sizing.fixed(32));

            this.onItemComponentCreated(parent, itemComponent, pattern, currentBanner, color);
            itemComponent.cursorStyle(CursorStyle.HAND);
            if (pattern instanceof NbtCompound patternCompound) {
                DyeColor patternColor = DyeColor.byId(patternCompound.getInt(TagsConstant.BANNER_PATTERN_COLOR));
                RegistryEntry<BannerPattern> patternRegistry = BannerPattern.byId(patternCompound.getString(TagsConstant.BANNER_PATTERN_VALUE));

                itemComponent.tooltip(BannerBuilder.tooltipOf(patternColor, patternRegistry));
            }

            bannerList.add(itemComponent);
        }
        this.patternsLayout.children(bannerList);
    }

    protected abstract void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color);
}
