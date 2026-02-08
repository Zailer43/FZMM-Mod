package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.history.IClipboardState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BannerBuilder implements IClipboardState {

    private final List<BannerPatternLayers.Layer> layers;
    private Item item;
    private boolean isShield;

    private BannerBuilder() {
        this.layers = new ArrayList<>();
        this.item = Items.WHITE_BANNER;
        this.isShield = false;
    }

    public static BannerBuilder builder() {
        return new BannerBuilder();
    }

    public static BannerBuilder of(ItemStack stack) {
        stack = stack.copy();

        DataComponentMap components = stack.getComponents();
        List<BannerPatternLayers.Layer> layers = components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers();
        DyeColor baseColor = components.getOrDefault(DataComponents.BASE_COLOR, null);

        Item item = stack.getItem();
        boolean isShield = item instanceof ShieldItem;

        if (baseColor != null) {
            item = getBannerByDye(baseColor);
        }

        return builder()
                .addLayers(layers)
                .item(item instanceof BannerItem ? item : Items.WHITE_BANNER)
                .isShield(isShield);
    }

    public ItemStack get() {
        ItemStack stack = this.item.getDefaultInstance();

        if (this.isShield) {
            stack = Items.SHIELD.getDefaultInstance();
            stack.update(DataComponents.BASE_COLOR, null, component -> this.baseBannerColor());
        }

        stack.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, component ->
                new BannerPatternLayers(new ArrayList<>(this.layers))
        );

        return stack;
    }

    public BannerBuilder item(Item item) {
        this.item = item;
        return this;
    }

    public BannerBuilder isShield(boolean isShield) {
        this.isShield = isShield;
        return this;
    }

    public boolean isShield() {
        return this.isShield;
    }

    public BannerBuilder addLayer(DyeColor color, Holder<BannerPattern> pattern) {
        this.addLayer(new BannerPatternLayers.Layer(pattern, color));
        return this;
    }

    public void addLayer(BannerPatternLayers.Layer layer) {
        this.layers.add(layer);
    }

    public BannerBuilder addLayers(List<BannerPatternLayers.Layer> layers) {
        this.layers.addAll(layers);
        return this;
    }

    public void removeLayer(BannerPatternLayers.Layer layer) {
        this.layers.remove(layer);
    }

    public void replaceColor(BannerPatternLayers.Layer layer, DyeColor color) {
        // avoid using List#indexOf because what is needed in this case is the reference,
        // otherwise you will get the wrong index
        int index = this.indexOf(layer);
        if (index != -1) {
            this.layers.set(index, new BannerPatternLayers.Layer(layer.pattern(), color));
        }
    }

    public int indexOf(BannerPatternLayers.Layer layer) {
        for (int i = 0; i != this.layers.size(); i++) {
            if (this.layers.get(i) == layer) {
                return i;
            }
        }
        return -1;
    }

    public void replaceColors(DyeColor colorToReplace, DyeColor newColor) {
        for (int i = 0; i != this.layers.size(); i++) {
            if (this.layers.get(i).color() == colorToReplace) {
                this.layers.set(i, new BannerPatternLayers.Layer(this.layers.get(i).pattern(), newColor));
            }
        }
    }

    public List<BannerPatternLayers.Layer> layers() {
        return this.layers;
    }

    public BannerBuilder clearPatterns() {
        this.layers.clear();

        return this;
    }

    @Override
    public BannerBuilder copy() {
        BannerBuilder copy = builder()
                .item(this.item)
                .isShield(this.isShield);

        for (var layer : this.layers) {
            copy.addLayer(new BannerPatternLayers.Layer(layer.pattern(), layer.color()));
        }

        return copy;
    }

    public static Item getBannerByDye(DyeColor color) {
        for (var block : BuiltInRegistries.BLOCK.stream().toList()) {
            if (block instanceof AbstractBannerBlock bannerBlock && bannerBlock.getColor() == color)
                return block.asItem();
        }

        return Items.WHITE_BANNER;
    }

    public void baseBannerColor(DyeColor color) {
        this.item(getBannerByDye(color));
    }

    public DyeColor baseBannerColor() {
        return baseBannerColor(this.item);
    }

    public static DyeColor baseBannerColor(Item item) {
        if (item instanceof BannerItem bannerItem)
            return bannerItem.getColor();

        return DyeColor.WHITE;
    }

    public static Component tooltipOf(BannerPatternLayers.Layer layer) {
        Optional<String> patternKeyOptional = layer.pattern().unwrapKey().map(key -> key.identifier().toShortLanguageKey());

        if (patternKeyOptional.isEmpty()) {
            FzmmClient.LOGGER.error("[BannerBuilder] No banner pattern translation key found");
            return Component.empty();
        }

        String dyeId = layer.color().getName();
        return Component.translatable("block.minecraft.banner." + patternKeyOptional.get() + "." + dyeId).withStyle(ChatFormatting.GRAY);
    }
}
