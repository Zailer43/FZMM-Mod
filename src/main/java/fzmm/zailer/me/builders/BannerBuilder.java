package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BannerBuilder {

    private final List<BannerPatternsComponent.Layer> layers;
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

        ComponentMap components = stack.getComponents();
        List<BannerPatternsComponent.Layer> layers = components.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT).layers();
        DyeColor baseColor = components.getOrDefault(DataComponentTypes.BASE_COLOR, null);

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
        ItemStack stack = this.item.getDefaultStack();

        if (this.isShield) {
            stack = Items.SHIELD.getDefaultStack();
            stack.apply(DataComponentTypes.BASE_COLOR, null, component -> this.baseBannerColor());
        }

        stack.apply(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT, component ->
                new BannerPatternsComponent(new ArrayList<>(this.layers))
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

    public BannerBuilder addLayer(DyeColor color, RegistryEntry<BannerPattern> pattern) {
        this.addLayer(new BannerPatternsComponent.Layer(pattern, color));
        return this;
    }

    public void addLayer(BannerPatternsComponent.Layer layer) {
        this.layers.add(layer);
    }

    public BannerBuilder addLayers(List<BannerPatternsComponent.Layer> layers) {
        this.layers.addAll(layers);
        return this;
    }

    public void removeLayer(BannerPatternsComponent.Layer layer) {
        this.layers.remove(layer);
    }

    public void replaceColor(BannerPatternsComponent.Layer layer, DyeColor color) {
        // avoid using List#indexOf because what is needed in this case is the reference,
        // otherwise you will get the wrong index
        int index = this.indexOf(layer);
        if (index != -1) {
            this.layers.set(index, new BannerPatternsComponent.Layer(layer.pattern(), color));
        }
    }

    public int indexOf(BannerPatternsComponent.Layer layer) {
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
                this.layers.set(i, new BannerPatternsComponent.Layer(this.layers.get(i).pattern(), newColor));
            }
        }
    }

    public List<BannerPatternsComponent.Layer> layers() {
        return this.layers;
    }

    public BannerBuilder clearPatterns() {
        this.layers.clear();

        return this;
    }

    public BannerBuilder copy() {
        BannerBuilder copy = builder()
                .item(this.item)
                .isShield(this.isShield);

        for (var layer : this.layers) {
            copy.addLayer(new BannerPatternsComponent.Layer(layer.pattern(), layer.color()));
        }

        return copy;
    }

    public static Item getBannerByDye(DyeColor color) {
        for (var block : Registries.BLOCK.stream().toList()) {
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

    public static Text tooltipOf(BannerPatternsComponent.Layer layer) {
        Optional<String> patternKeyOptional = layer.pattern().getKey().map(key -> key.getValue().toShortTranslationKey());

        if (patternKeyOptional.isEmpty()) {
            FzmmClient.LOGGER.error("[BannerBuilder] No banner pattern translation key found");
            return Text.empty();
        }

        String dyeId = layer.color().getName();
        return Text.translatable("block.minecraft.banner." + patternKeyOptional.get() + "." + dyeId).formatted(Formatting.GRAY);
    }
}
