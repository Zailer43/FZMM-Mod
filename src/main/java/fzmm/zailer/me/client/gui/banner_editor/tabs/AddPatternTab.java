package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddPatternTab implements IBannerTab {

    @Override
    public String buttonId() {
        return "add-pattern";
    }

    @Override
    public List<Component>  update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color) {
        List<Component> bannerList = new ArrayList<>();

        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
        Optional<Registry<BannerPattern>> bannerRegistry = registryManager.getOptional(RegistryKeys.BANNER_PATTERN);
        if (bannerRegistry.isEmpty()) {
            FzmmClient.LOGGER.error("[AddPatternTab] No banner registry found");
            return bannerList;
        }

        RegistryKey<BannerPattern> basePattern = BannerPatterns.BASE;

        for (var registry : bannerRegistry.stream().toList()) {
            for (var pattern : registry.streamEntries().toList()) {
                if (basePattern == pattern.registryKey()) {
                    continue;
                }

                ItemStack banner = currentBanner.copy()
                        .addLayer(color, pattern)
                        .get();

                Component itemComponent = EComponents.item(banner)
                        .sizing(Sizing.fixed(32), Sizing.fixed(32))
                        .tooltip(BannerBuilder.tooltipOf(new BannerPatternsComponent.Layer(pattern, color)));

                itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    UISounds.playButtonSound();
                    clipboard.addUndo(currentBanner);

                    currentBanner.addLayer(color, pattern);

                    clipboard.change(currentBanner);
                    return true;
                });
                itemComponent.cursorStyle(CursorStyle.HAND);

                bannerList.add(itemComponent);
            }
        }

        return bannerList;
    }
}
