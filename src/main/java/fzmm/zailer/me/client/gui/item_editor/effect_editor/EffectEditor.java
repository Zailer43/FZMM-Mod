package fzmm.zailer.me.client.gui.item_editor.effect_editor;

import fzmm.zailer.me.builders.EffectBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AppliedLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.effect_editor.components.AppliedEffectComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class EffectEditor extends LevelableEditor<StatusEffect, EffectBuilder.EffectData, EffectBuilder> implements IItemEditorScreen {
    private boolean showParticles;

    public EffectEditor() {
        super(EffectBuilder.builder());
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.POTION.getDefaultStack();
    }

    @Override
    public String getId() {
        return "effect";
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        FlowLayout layout = super.getLayout(baseScreen, editorLayout);

        this.showParticles = true;

        // boolean buttons
        FlowLayout booleanButtonsLayout = editorLayout.childById(FlowLayout.class, "boolean-buttons");
        BaseFzmmScreen.checkNull(booleanButtonsLayout, "flow-layout", "boolean-buttons");

        ButtonComponent showParticlesButton = Components.button(Text.empty(), buttonComponent -> {
            this.showParticles = !this.showParticles;
            this.levelableBuilder.showParticles(this.showParticles);
            this.updateItemPreview();
        });
        showParticlesButton.tooltip(Text.translatable("fzmm.gui.itemEditor.effect.option.showParticles"));
        this.setBooleanButton(showParticlesButton, () -> this.showParticles, 176);

        ButtonComponent ignoreMaxLevelComponent = Components.button(Text.empty(), buttonComponent -> {
            this.ignoreMaxLevel = !this.ignoreMaxLevel;

            this.setLevelRange(0, effectData -> this.getMaxLevel(Short.MAX_VALUE));
        });
        BaseFzmmScreen.checkNull(ignoreMaxLevelComponent, "button", "ignore-max-level");
        ignoreMaxLevelComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.levelable.option.ignoreMaxLevel"));
        this.setBooleanButton(ignoreMaxLevelComponent, () -> this.ignoreMaxLevel, 112);

        booleanButtonsLayout.children(List.of(showParticlesButton, ignoreMaxLevelComponent));

        // top options
        FlowLayout topOptionsLayout = editorLayout.childById(FlowLayout.class, "top-options");
        BaseFzmmScreen.checkNull(topOptionsLayout, "flow-layout", "top-options");

        LabelComponent labelComponent = Components.label(Text.translatable("fzmm.gui.itemEditor.effect.label.setDurationToAll"));
        ConfigTextBox setDurationsTextBox = new ConfigTextBox();
        AppliedEffectComponent.configureForTime(setDurationsTextBox);
        setDurationsTextBox.horizontalSizing(Sizing.fixed(40));
        setDurationsTextBox.setText("10s");
        setDurationsTextBox.setCursorToStart(false);
        setDurationsTextBox.onChanged().subscribe(value -> this.setDurationsExecute(setDurationsTextBox));

        topOptionsLayout.child(labelComponent);
        topOptionsLayout.child(setDurationsTextBox);

        return layout;
    }

    @Override
    protected void updateParameters(EffectBuilder builder) {
        this.showParticles = builder.showParticles();
        this.ignoreMaxLevel = builder.isOverMaxLevel();
    }

    private void setDurationsExecute(ConfigTextBox setDurationsTextBox) {
        if (!setDurationsTextBox.isValid())
            return;

        String value = setDurationsTextBox.getText();
        for (var child : this.getAppliedLevelablesLayout().children()) {
            if (child instanceof AppliedEffectComponent component) {
                component.setUpdateItemCallback(false);
                component.setDuration(value);
                component.setUpdateItemCallback(true);
            }
        }
        this.updateItemPreview();
    }

    @Override
    protected int getMaxLevel(int level) {
        return this.ignoreMaxLevel ? 255 : super.getMaxLevel(level);
    }

    @Override
    protected List<ButtonComponent> addCategories() {
        final String baseTranslationKey = "fzmm.gui.itemEditor.effect.category.";
        List<ButtonComponent> buttonList = new ArrayList<>();

        ButtonComponent allButton = Components.button(Text.translatable("fzmm.gui.button.category.all"),
                buttonComponent ->  this.applyCategory((levelable, itemStack) -> true, buttonList, buttonComponent));
        buttonList.add(allButton);
        allButton.onPress(); // default category


        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "beneficial"), buttonComponent -> {
           ILevelablePredicate<StatusEffect> predicate = (effect, itemStack) -> effect.isBeneficial();
           this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "harmful"), buttonComponent -> {
            ILevelablePredicate<StatusEffect> predicate = (effect, itemStack) -> effect.getCategory() == StatusEffectCategory.HARMFUL;
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "neutral"), buttonComponent -> {
            ILevelablePredicate<StatusEffect> predicate = (effect, itemStack) -> effect.getCategory() == StatusEffectCategory.NEUTRAL;
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "instant"), buttonComponent -> {
            ILevelablePredicate<StatusEffect> predicate = (effect, itemStack) -> effect.isInstant();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        this.addModsLevelablesToCategories(buttonList);

        for (var button : buttonList) {
            button.horizontalSizing(Sizing.fill(100));
            button.renderer(ButtonComponent.Renderer.flat(0x20000000, 0x40000000, 0x80000000));
        }

        return buttonList;
    }

    @Override
    protected AppliedLevelableComponent<StatusEffect, EffectBuilder.EffectData, EffectBuilder> getAppliedLevelableComponent(EffectBuilder.EffectData levelable, @Nullable Runnable callback) {
        var value = new AppliedEffectComponent(levelable, callback, this, this.levelableBuilder);
        value.setLevelRange(0, this.getMaxLevel(Short.MAX_VALUE));

        return value;
    }

    @Override
    public Text getLevelableName(EffectBuilder.EffectData levelable) {
        return levelable.getName();
    }

    @Override
    public Registry<StatusEffect> getRegistry() {
        return Registries.STATUS_EFFECT;
    }

    @Override
    public List<EffectBuilder.EffectData> getEntries() {
        Registry<StatusEffect> registry = this.getRegistry();
        List<EffectBuilder.EffectData> enchantments = new ArrayList<>();

        for (var entry : registry.getEntrySet()) {
            enchantments.add(new EffectBuilder.EffectData(entry.getValue(), 0));
        }

        return enchantments;
    }

    @Override
    protected boolean disableFilter(EffectBuilder.EffectData levelable) {
        return false;
    }
}
