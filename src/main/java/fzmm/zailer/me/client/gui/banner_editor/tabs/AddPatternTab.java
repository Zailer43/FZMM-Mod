package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddPatternTab implements IBannerTab {

    @Override
    public String buttonId() {
        return "add-pattern";
    }

    @Override
    public List<UIComponent>  update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color) {
        List<UIComponent> bannerList = new ArrayList<>();

        RegistryAccess registryManager = FzmmUtils.getRegistryManager();
        Optional<Registry<BannerPattern>> bannerRegistry = registryManager.lookup(Registries.BANNER_PATTERN);
        if (bannerRegistry.isEmpty()) {
            FzmmClient.LOGGER.error("[AddPatternTab] No banner registry found");
            return bannerList;
        }

        ResourceKey<BannerPattern> basePattern = BannerPatterns.BASE;

        for (var registry : bannerRegistry.stream().toList()) {
            for (var pattern : registry.listElements().toList()) {
                if (basePattern == pattern.key()) {
                    continue;
                }

                ItemStack banner = currentBanner.copy()
                        .addLayer(color, pattern)
                        .get();

                UIComponent itemComponent = EComponents.item(banner)
                        .sizing(Sizing.fixed(32), Sizing.fixed(32))
                        .tooltip(BannerBuilder.tooltipOf(new BannerPatternLayers.Layer(pattern, color)));

                itemComponent.mouseDown().subscribe((input, doubled) -> {
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
