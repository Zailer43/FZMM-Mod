package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EffectBuilder implements ILevelableBuilder<StatusEffect, EffectBuilder.EffectData> {
    private ItemStack stack;
    private final List<EffectData> effects;
    private boolean allowDuplicates;
    private boolean showParticles;

    private EffectBuilder() {
        this.stack = Items.POTION.getDefaultStack();
        this.effects = new ArrayList<>();
        this.allowDuplicates = false;
        this.showParticles = true;
    }

    public static EffectBuilder builder() {
        return new EffectBuilder();
    }

    @Override
    public ItemStack get() {
        NbtList effectList = new NbtList();

        if (!this.allowDuplicates)
            this.removeDuplicates();

        for (var effectData : this.effects) {
            effectData.showParticles(this.showParticles);
            effectList.add(effectData.createNbt());
        }

        if (effectList.isEmpty())
            this.stack.removeSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY);
        else
            this.stack.setSubNbt(PotionUtil.CUSTOM_POTION_EFFECTS_KEY, effectList);

        return this.stack.copy();
    }

    @Override
    public EffectBuilder add(EffectBuilder.EffectData value) {
        this.effects.add(value);
        return this;
    }

    public EffectBuilder add(StatusEffectInstance effect, Identifier id) {
        return this.add(new EffectData(effect, id));
    }

    public EffectBuilder addAll(NbtList effects) {
        for (var element : effects) {
            if (element instanceof NbtCompound compound) {
                StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(compound);
                Identifier id = null;

                // writes the nbt with an effect that will be ignored,
                // because I need to use the identifier and not the effect,
                // since the identifier is going to be written
                if (effectInstance == null && compound.contains(TagsConstant.EFFECT_ID)) {
                    NbtCompound compoundCopy = compound.copy();

                    id = Identifier.tryParse(compoundCopy.getString(TagsConstant.EFFECT_ID));
                    Identifier effectId =  Registries.STATUS_EFFECT.getId(StatusEffects.ABSORPTION);
                    assert effectId != null;

                    compoundCopy.putString(TagsConstant.EFFECT_ID, effectId.toString());
                    effectInstance = StatusEffectInstance.fromNbt(compoundCopy);
                }

                if (effectInstance == null)
                    continue;

                if (id == null)
                    id = Registries.STATUS_EFFECT.getId(effectInstance.getEffectType());

                this.add(effectInstance, id);
            }
        }

        return this;
    }

    @Override
    public EffectBuilder remove(int index) {
        this.effects.remove(index);
        return this;
    }

    @Override
    public int getMaxLevel(int level) {
        // currently, the maximum level of the effects is Byte.MAX_VALUE since 1.20.2,
        // previously level 255 was supported, but now this level is like level 1.
        return Math.min(level, Byte.MAX_VALUE);
    }

    @Override
    public boolean isOverMaxLevel() {
        for (var effectData : this.effects) {
            if (this.getMaxLevel(effectData.amplifier) != effectData.amplifier)
                return true;
        }

        return false;
    }

    @Override
    public EffectBuilder allowDuplicates(boolean value) {
        this.allowDuplicates = value;
        return this;
    }

    @Override
    public boolean allowDuplicates() {
        return this.allowDuplicates;
    }

    public boolean hasDuplicates() {
        for (int i = 0; i != this.effects.size(); i++) {
            for (int j = i + 1; j != this.effects.size(); j++) {
                if (i != j && this.effects.get(i).getValue() == this.effects.get(j).getValue())
                    return true;
            }
        }
        return false;
    }

    public EffectBuilder removeDuplicates() {
        List<EffectData> newEffects = new ArrayList<>();

        for (var effectData : this.effects) {
            boolean found = false;
            for (var newEffect : newEffects) {
                if (effectData.getValue().equals(newEffect.getValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newEffects.add(effectData);
            }
        }
        this.effects.clear();
        this.effects.addAll(newEffects);

        return this;
    }

    @Override
    public EffectBuilder clear() {
        this.effects.clear();
        return this;
    }

    @Override
    public EffectBuilder stack(ItemStack stack) {
        this.stack = stack.copy();

        NbtCompound compound = this.stack.getOrCreateNbt();
        NbtList effects = compound.getList(PotionUtil.CUSTOM_POTION_EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
        this.clear().addAll(effects);

        this.initShowParticles();

        return this.allowDuplicates(this.hasDuplicates());
    }

    @Override
    public ItemStack stack() {
        return this.stack;
    }

    private void initShowParticles() {
        for (var effectData : this.effects) {
            if (!effectData.showParticles()) {
                this.showParticles(false);
                return;
            }
        }
        this.showParticles(true);
    }


    @Override
    public List<EffectData> values() {
        return new ArrayList<>(this.effects);
    }

    @Override
    public EffectBuilder values(List<EffectData> values) {
        this.effects.clear();
        this.effects.addAll(values);
        return this;
    }

    @Override
    public EffectData getValue(int index) {
        return this.effects.get(index);
    }

    @Override
    public boolean contains(StatusEffect value) {
        for (var effectData : this.effects) {
            Optional<StatusEffect> effectOptional = effectData.getValue();
            if (effectOptional.isPresent() && effectOptional.get() == value)
                return true;
        }
        return false;
    }

    public boolean showParticles() {
        return this.showParticles;
    }

    public void showParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    public static class EffectData implements ILevelable<StatusEffect> {

        @Nullable
        private final StatusEffect effect;
        private final Identifier id;
        private int amplifier;
        private boolean ambient;
        private int duration;
        private boolean showParticles;
        private boolean showIcon;

        public EffectData(StatusEffectInstance effectInstance, Identifier id) {
            this.effect = Registries.STATUS_EFFECT.get(id) == effectInstance.getEffectType() ? effectInstance.getEffectType() : null;
            this.id = id;
            this.amplifier = effectInstance.getAmplifier();
            this.ambient = effectInstance.isAmbient();
            this.duration = effectInstance.getDuration();
            this.showParticles = effectInstance.shouldShowParticles();
            this.showIcon = effectInstance.shouldShowIcon();
        }

        public EffectData(@Nullable StatusEffect effect, Identifier id, int amplifier) {
            this.effect = effect;
            this.id = id;
            this.amplifier = amplifier;
            this.ambient = false;
            this.duration = 200;
            this.showParticles = true;
            this.showIcon = true;
        }

        @Override
        public Optional<StatusEffect> getValue() {
            return Optional.ofNullable(this.effect);
        }

        @Override
        public Identifier valueId() {
            return this.id;
        }

        @Override
        public Text getName() {
            return this.getValue()
                    .map(StatusEffect::getName)
                    .orElseGet(() -> Text.literal(this.id.toString()));
        }

        @Override
        public int getLevel() {
            return this.amplifier;
        }

        @Override
        public void setLevel(int level) {
            this.amplifier = level;
        }

        @Override
        public String getTranslationKey() {
            return this.getValue()
                    .map(StatusEffect::getTranslationKey)
                    .orElseGet(this.id::toTranslationKey);
        }

        @Override
        public boolean isAcceptableItem(ItemStack stack) {
            Item item = stack.getItem();
            return item instanceof PotionItem || item instanceof TippedArrowItem;
        }

        @Override
        public @Nullable Sprite getSprite() {
            return this.getValue()
                    .map(statusEffect -> MinecraftClient.getInstance().getStatusEffectSpriteManager().getSprite(statusEffect))
                    .orElse(null);
        }

        @Override
        public boolean canHaveSprite() {
            return true;
        }

        public boolean ambient() {
            return this.ambient;
        }

        public void ambient(boolean ambient) {
            this.ambient = ambient;
        }

        public boolean showParticles() {
            return this.showParticles;
        }

        public void showParticles(boolean showParticles) {
            this.showParticles = showParticles;
        }

        public boolean showIcon() {
            return this.showIcon;
        }

        public void showIcon(boolean showIcon) {
            this.showIcon = showIcon;
        }

        public int duration() {
            return this.duration;
        }

        public void duration(int duration) {
            this.duration = duration;
        }

        private NbtCompound createNbt() {
            NbtCompound compound = new NbtCompound();

            // write the nbt with an effect that later will be overwritten,
            // because I need to use the identifier and not the effect,
            // since in case the effect is null it will not use the identifier
            StatusEffectInstance instance = new StatusEffectInstance(StatusEffects.ABSORPTION, this.duration, this.amplifier,
                    this.ambient, this.showParticles, this.showIcon);
            instance.writeNbt(compound);

            compound.putString(TagsConstant.EFFECT_ID, this.id.toString());

            return compound;
        }
    }
}
