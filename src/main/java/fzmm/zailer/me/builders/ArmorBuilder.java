package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ArmorBuilder {

    private NbtCompound nbt = new NbtCompound();
    @Nullable
    private ArmorTrimMaterial trimMaterial = null;
    @Nullable
    private ArmorTrimPattern trimPattern = null;
    private Item item = Items.AIR;
    private boolean hasTrim = false;
    private int count = 1;

    private ArmorBuilder() {
    }

    public static ArmorBuilder builder() {
        return new ArmorBuilder();
    }

    public ArmorBuilder of(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        this.item(stack.getItem())
                .nbt(nbt);

        this.trimMaterial = null;
        this.trimPattern = null;
        this.count = stack.getCount();

        if (!nbt.contains(TagsConstant.TRIM_COMPOUND))
            return this;

        World world = MinecraftClient.getInstance().world;
        assert world != null;
        DynamicRegistryManager registryManager = world.getRegistryManager();

        Optional<ArmorTrim> armorTrimOptional = ArmorTrim.getTrim(registryManager, stack, true);
        this.hasTrim = armorTrimOptional.isPresent();
        if (this.hasTrim) {
            ArmorTrim armorTrim = armorTrimOptional.get();
            this.trimMaterial = armorTrim.getMaterial().value();
            this.trimPattern = armorTrim.getPattern().value();
        }

        return this;
    }

    public ArmorBuilder of(ArmorBuilder builder) {
        return this.nbt(builder.nbt.copy())
                .item(builder.item())
                .trimMaterial(builder.trimMaterial())
                .trimPattern(builder.trimPattern())
                .count(builder.count());
    }

    public ArmorBuilder copy() {
        return ArmorBuilder.builder()
                .nbt(this.nbt.copy())
                .item(this.item)
                .trimMaterial(this.trimMaterial)
                .trimPattern(this.trimPattern)
                .hasTrim(this.hasTrim)
                .count(this.count);
    }

    public ArmorBuilder nbt(NbtCompound nbt) {
        this.nbt = nbt;
        return this;
    }

    public ArmorBuilder item(Item item) {
        this.item = item;
        this.count(Math.max(1, this.count));
        return this;
    }

    public ArmorBuilder count(int count) {
        this.count = count;
        return this;
    }

    public int count() {
        return this.count;
    }

    public Item item() {
        return this.item;
    }

    public ArmorBuilder trimMaterial(@Nullable ArmorTrimMaterial material) {
        this.trimMaterial = material;
        return this;
    }

    public ArmorTrimMaterial trimMaterial() {
        return this.trimMaterial;
    }

    public ArmorBuilder trimPattern(@Nullable ArmorTrimPattern pattern) {
        this.trimPattern = pattern;
        return this;
    }

    public ArmorTrimPattern trimPattern() {
        return this.trimPattern;
    }

    public ItemStack get() {
        NbtCompound nbt = this.nbt.copy();

        assert MinecraftClient.getInstance().world != null;
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        ItemStack stack = this.item.getDefaultStack();
        stack.setNbt(nbt);
        stack.setCount(this.count);

        if (this.trimMaterial == null || this.trimPattern == null) {
            stack.removeSubNbt(TagsConstant.TRIM_COMPOUND);
            this.hasTrim = false;
        } else {
            RegistryEntry<ArmorTrimMaterial> trimMaterial = registryManager.get(RegistryKeys.TRIM_MATERIAL).getEntry(this.trimMaterial);
            RegistryEntry<ArmorTrimPattern> trimPattern = registryManager.get(RegistryKeys.TRIM_PATTERN).getEntry(this.trimPattern);
            ArmorTrim trim = new ArmorTrim(trimMaterial, trimPattern);
            this.hasTrim = ArmorTrim.apply(registryManager, stack, trim);
        }

        return stack;
    }

    public boolean hasTrim() {
        return this.hasTrim;
    }

    private ArmorBuilder hasTrim(boolean hasTrim) {
        this.hasTrim = hasTrim;
        return this;
    }
}
