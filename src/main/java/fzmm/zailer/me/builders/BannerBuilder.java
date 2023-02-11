package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BannerBuilder {

    private final NbtList patterns;
    private Item item;
    private boolean isShield;
    private NbtCompound nbt;

    private BannerBuilder() {
        this.patterns = new NbtList();
        this.item = Items.WHITE_BANNER;
        this.isShield = false;
        this.nbt = new NbtCompound();
    }

    public static BannerBuilder builder() {
        return new BannerBuilder();
    }

    public static BannerBuilder of(ItemStack stack) {
        stack = stack.copy();
        // don't ask me why air can and has nbt
        NbtCompound nbt = stack.isEmpty() ? new NbtCompound() : stack.getOrCreateNbt();
        NbtCompound blockEntityTag = nbt.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE) ? nbt.getCompound(TagsConstant.BLOCK_ENTITY) : new NbtCompound();
        NbtList patterns = new NbtList();

        if (blockEntityTag.contains(TagsConstant.BANNER_PATTERN, NbtElement.LIST_TYPE))
            patterns = blockEntityTag.getList(TagsConstant.BANNER_PATTERN, NbtElement.COMPOUND_TYPE);

        Item item = stack.getItem();
        boolean isShield = item instanceof ShieldItem;
        if (isShield)
            item = getBannerByDye(ShieldItem.getColor(stack));

        return builder()
                .addPatterns(patterns)
                .item(item instanceof BannerItem ? item : Items.WHITE_BANNER)
                .isShield(isShield)
                .nbt(nbt);
    }

    public ItemStack get() {
        ItemStack stack = this.item.getDefaultStack();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList patterns = new NbtList();

        if (this.isShield) {
            stack = Items.SHIELD.getDefaultStack();
            Block block = Block.getBlockFromItem(this.item);
            if (!(block instanceof AbstractBannerBlock)) {
                FzmmClient.LOGGER.error("[Banner builder] Item {} is not a banner type, the white banner will be used instead.", this.item.getName());
                block = Blocks.WHITE_BANNER;
            }
            assert block instanceof AbstractBannerBlock;
            int color = ((AbstractBannerBlock) block).getColor().getId();

            blockEntityTag.putInt(ShieldItem.BASE_KEY, color);
        }

        this.formatPatterns();
        patterns.addAll(this.patterns);

        blockEntityTag.put(TagsConstant.BANNER_PATTERN, patterns);
        this.nbt.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);

        stack.setNbt(this.nbt);
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

    public BannerBuilder nbt(NbtCompound nbt) {
        this.nbt = nbt;
        return this;
    }

    public BannerBuilder addPattern(DyeColor color, RegistryKey<BannerPattern> patternRegistry) {
        BannerPattern pattern = Registries.BANNER_PATTERN.get(patternRegistry);

        if (pattern == null) {
            FzmmClient.LOGGER.error("[Banner builder] No banner pattern found '{}'", patternRegistry.getValue());
            return this;
        }

        return this.addPattern(color, pattern);
    }

    public BannerBuilder addPattern(DyeColor color, BannerPattern pattern) {
        return this.addPattern(color.getId(), pattern.getId());
    }

    public BannerBuilder addPattern(int color, String pattern) {
        this.addPattern(this.getPattern(color, pattern));
        return this;
    }

    public void addPattern(NbtElement pattern) {
        this.patterns.add(pattern);
    }

    public BannerBuilder addPatterns(NbtList patterns) {
        this.patterns.addAll(patterns);
        return this;
    }

    public void removePattern(NbtElement pattern) {
        this.patterns.remove(pattern);
    }

    private NbtCompound getPattern(int color, String pattern) {
        NbtCompound patternCompound = new NbtCompound();

        patternCompound.putInt(TagsConstant.BANNER_PATTERN_COLOR, color);
        patternCompound.putString(TagsConstant.BANNER_PATTERN_VALUE, pattern);

        return patternCompound;
    }

    public NbtList patterns() {
        return this.patterns;
    }

    private void formatPatterns() {
        BannerPattern basePattern = Registries.BANNER_PATTERN.get(BannerPatterns.BASE);
        if (basePattern == null || this.patterns.size() == 0)
            return;

        NbtElement firstPatternElement = this.patterns.get(0);
        if (!(firstPatternElement instanceof NbtCompound firstPattern))
            return;

        if (firstPattern.getString(TagsConstant.BANNER_PATTERN_VALUE).equals(basePattern.getId())) {
            int color = firstPattern.getInt(TagsConstant.BANNER_PATTERN_COLOR);
            this.bannerColor(DyeColor.byId(color));
            this.patterns.remove(firstPatternElement);
        }
    }

    public BannerBuilder clearPatterns() {
        this.patterns.clear();

        return this;
    }

    public BannerBuilder copy() {
        BannerBuilder copy = builder()
                .item(this.item)
                .isShield(this.isShield);

        for (var pattern : this.patterns) {
            if (pattern instanceof NbtCompound patternCompound) {
                copy.addPattern(patternCompound.getInt(TagsConstant.BANNER_PATTERN_COLOR),
                        patternCompound.getString(TagsConstant.BANNER_PATTERN_VALUE));
            }
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

    public void bannerColor(DyeColor color) {
        this.item(getBannerByDye(color));
    }

    public DyeColor bannerColor() {
        if (this.item instanceof BannerItem bannerItem)
            return bannerItem.getColor();

        return DyeColor.WHITE;
    }

    public static Text tooltipOf(DyeColor color, @Nullable RegistryEntry<BannerPattern> patternRegistry) {
        if (patternRegistry == null) {
            FzmmClient.LOGGER.error("[Banner builder] No banner pattern found");
            return Text.empty();
        }

        Optional<String> patternKeyOptional = patternRegistry.getKey().map(key -> key.getValue().toShortTranslationKey());

        if (patternKeyOptional.isEmpty()) {
            FzmmClient.LOGGER.error("[Banner builder] No banner pattern translation key found");
            return Text.empty();
        }

        return Text.translatable("block.minecraft.banner." + patternKeyOptional.get() + "." + color.getName()).formatted(Formatting.GRAY);
    }
}
