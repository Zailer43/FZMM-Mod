package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentBuilder {

    private ItemStack stack;
    private final List<EnchantmentData> enchantments;
    private boolean glint;
    private boolean allowDuplicates;

    private EnchantmentBuilder() {
        this.enchantments = new ArrayList<>();
        this.glint = false;
        this.allowDuplicates = true;
        this.stack = Items.DIAMOND_SWORD.getDefaultStack();
    }

    public static EnchantmentBuilder builder() {
        return new EnchantmentBuilder();
    }

    public EnchantmentBuilder add(Enchantment enchantment, int level) {
        this.enchantments.add(new EnchantmentData(enchantment, level));
        return this;
    }

    public EnchantmentBuilder add(Enchantment enchantment) {
        this.add(enchantment, 1);
        return this;
    }

    public EnchantmentBuilder addAll(NbtList enchantments) {
        for (var element : enchantments) {
            if (element instanceof NbtCompound compound) {
                Identifier enchantId = EnchantmentHelper.getIdFromNbt(compound);
                if (enchantId == null || enchantId.getPath().isEmpty())
                    continue;

                // is not used EnchantmentHelper.getLevelFromNbt() because it makes a MathHelper.clamp
                int level = compound.getShort(TagsConstant.ENCHANTMENTS_LVL);
                this.add(Registries.ENCHANTMENT.get(enchantId), level);
            }
        }

        return this;
    }

    public EnchantmentBuilder addAll(List<Enchantment> enchantments, int level) {
        for (Enchantment enchantment : enchantments)
            this.add(enchantment, level);
        return this;
    }

    public EnchantmentBuilder remove(Enchantment enchantment) {
        this.enchantments.removeIf(enchantmentData -> enchantmentData.getEnchantment() == enchantment);
        return this;
    }

    public EnchantmentBuilder remove(int index) {
        if (index >= 0 && index < this.enchantments.size())
            this.enchantments.remove(index);
        return this;
    }

    public EnchantmentBuilder removeAll() {
        this.enchantments.clear();
        return this;
    }

    public EnchantmentBuilder clear() {
        this.enchantments.clear();
        return this;
    }

    public EnchantmentData getEnchant(int index) {
        if (index >= 0 && index < this.enchantments.size())
            return this.enchantments.get(index);
        return new EnchantmentData(Enchantments.AQUA_AFFINITY, 1);
    }

    public List<EnchantmentData> enchantments() {
        return new ArrayList<>(this.enchantments);
    }

    public void enchantments(List<EnchantmentData> enchantments) {
        this.enchantments.clear();
        this.enchantments.addAll(enchantments);
    }

    public boolean contains(Enchantment enchantment) {
        for (var enchantmentData : this.enchantments)
            if (enchantmentData.getEnchantment() == enchantment)
                return true;
        return false;
    }

    public EnchantmentBuilder setLevel(int index, int level) {
        if (index >= 0 && index < this.enchantments.size())
            this.enchantments.get(index).setLevel(level);
        return this;
    }

    public EnchantmentBuilder removeDuplicates() {
        List<EnchantmentData> newEnchantments = new ArrayList<>();

        for (var enchantmentData : this.enchantments) {
            if (!newEnchantments.contains(enchantmentData)) {
                newEnchantments.add(enchantmentData);
            }
        }

        this.enchantments.clear();
        this.enchantments.addAll(newEnchantments);

        return this;
    }

    public boolean isCompatibleWith(Enchantment enchantment) {
        for (var enchantmentData : this.enchantments) {
            Enchantment enchant = enchantmentData.getEnchantment();
            if (enchantment != enchant && !enchant.canCombine(enchantment))
                return false;
        }
        return true;
    }

    public boolean hasDuplicates() {
        for (int i = 0; i != this.enchantments.size(); i++) {
            for (int j = i + 1; j != this.enchantments.size(); j++) {
                if (i != j && this.enchantments.get(i).getEnchantment() == this.enchantments.get(j).getEnchantment())
                    return true;
            }
        }
        return false;
    }

    public boolean isOverMaxLevel() {
        for (var enchantmentData : this.enchantments) {
            if (getMaxLevel(enchantmentData.level) != enchantmentData.level)
                return true;
        }
        return false;
    }

    public boolean onlyCompatibleEnchants() {
        for (var enchantmentData : this.enchantments) {
            if (!this.isCompatibleWith(enchantmentData.getEnchantment()))
                return false;
        }
        return true;
    }

    public EnchantmentBuilder allowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        return this;
    }

    public boolean allowDuplicates() {
        return this.allowDuplicates;
    }

    public EnchantmentBuilder stack(ItemStack stack) {
        this.stack = stack.copy();

        NbtCompound compound = this.stack.getOrCreateNbt();
        NbtList enchants = compound.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        this.addAll(enchants);

        this.glint(enchants.size() == 1 && enchants.get(0) instanceof NbtCompound nbt && nbt.isEmpty());

        return this.allowDuplicates(this.hasDuplicates());
    }

    public ItemStack stack() {
        return this.stack;
    }

    public EnchantmentBuilder glint(boolean glint) {
        this.glint = glint;
        return this;
    }

    public boolean glint() {
        return this.glint;
    }

    public ItemStack get() {
        NbtList enchants = new NbtList();

        if (!this.allowDuplicates)
            this.removeDuplicates();

        if (this.glint && this.enchantments.isEmpty()) {
            enchants.add(new NbtCompound());
        } else {
            for (var enchantmentData : this.enchantments)
                enchants.add(enchantmentData.createNbt());
        }

        this.stack.setSubNbt(ItemStack.ENCHANTMENTS_KEY, enchants);

        return this.stack;
    }

    public static int getMaxLevel(int level) {
        NbtCompound nbt = new NbtCompound();
        // I didn't see a static where the max level is declared,
        // so doing this should at least avoid breaking compatibility
        // if another mod decides to change the max level of the enchants
        EnchantmentHelper.writeLevelToNbt(nbt, level);
        return EnchantmentHelper.getLevelFromNbt(nbt);
    }

    public static final class EnchantmentData {
        private final Enchantment enchantment;
        private int level;

        public EnchantmentData(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public NbtCompound createNbt() {
            int level = MathHelper.clamp(this.level, Short.MIN_VALUE, Short.MAX_VALUE); // avoid short overflow
            return EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(this.enchantment), level);
        }

        public Enchantment getEnchantment() {
            return this.enchantment;
        }

        public String getName() {
            return this.enchantment.getName(1).getString();
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

    }
}
