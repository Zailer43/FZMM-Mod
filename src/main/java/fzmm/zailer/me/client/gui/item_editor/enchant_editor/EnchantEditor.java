package fzmm.zailer.me.client.gui.item_editor.enchant_editor;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.components.ScrollableButtonComponent;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AddLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AppliedLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.AddEnchantComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EnchantEditor extends LevelableEditor<Enchantment, EnchantmentBuilder.EnchantmentData, EnchantmentBuilder> implements IItemEditorScreen {
    private boolean glint;
    private boolean onlyCompatibleEnchants;
    private EnumWidget levelRangeEnumComponent;


    public EnchantEditor() {
        super(EnchantmentBuilder.builder());
    }

    @Override
    public ItemStack getExampleItem() {
        return EnchantmentBuilder.builder()
                .stack(Items.ENCHANTED_BOOK.getDefaultStack())
                .glint(true)
                .get();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.glint = false;
        this.onlyCompatibleEnchants = false;

        FlowLayout layout = super.getLayout(baseScreen, editorLayout);

        // top buttons
        FlowLayout booleanLayout = layout.childById(FlowLayout.class, "boolean-buttons");
        BaseFzmmScreen.checkNull(booleanLayout, "booleanLayout", "boolean-buttons");

        ButtonComponent glintComponent = Components.button(Text.empty(), buttonComponent -> {
            this.glint = !this.glint;
            this.levelableBuilder.glint(this.glint);
            this.updateItemPreview();
        });
        glintComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.glint"));
        this.setBooleanButton(glintComponent, () -> this.glint, 48);

        ButtonComponent onlyCompatibleEnchantsComponent = Components.button(Text.empty(), buttonComponent -> {
            this.onlyCompatibleEnchants = !this.onlyCompatibleEnchants;
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });
        onlyCompatibleEnchantsComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.onlyCompatibleEnchants"));
        this.setBooleanButton(onlyCompatibleEnchantsComponent, () -> this.onlyCompatibleEnchants, 144);

        Function<Text, Text> getLevelRangeText = text -> Text.translatable("fzmm.gui.itemEditor.effect.option.maxLevel", text);
        this.levelRangeEnumComponent = new EnumWidget() {
            @Override
            public void setMessage(Text message) {
                super.setMessage(getLevelRangeText.apply(message));
            }
        };
        this.levelRangeEnumComponent.init(LevelRange.BYTE);
        this.levelRangeEnumComponent.onPress(buttonComponent -> {
            LevelRange levelRange = (LevelRange) this.levelRangeEnumComponent.getValue();

            this.setLevelRange(levelRange.getMinLevel(), levelRange.maxLevelFunc);
        });
        int levelRangeEnumWidth = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (var value : LevelRange.values()) {
            Text translation = getLevelRangeText.apply(Text.translatable(value.getTranslationKey()));
            levelRangeEnumWidth = Math.max(levelRangeEnumWidth, textRenderer.getWidth(translation));
        }
        this.levelRangeEnumComponent.horizontalSizing(Sizing.fixed(levelRangeEnumWidth + BaseFzmmScreen.BUTTON_TEXT_PADDING));


        booleanLayout.children(List.of(glintComponent, onlyCompatibleEnchantsComponent, this.levelRangeEnumComponent));

        return layout;
    }

    @Override
    public String getId() {
        return "enchant";
    }

    @Override
    protected void updateParameters(EnchantmentBuilder builder) {
        this.glint = builder.glint();
        this.onlyCompatibleEnchants = builder.onlyCompatibleEnchants();
        this.levelRangeEnumComponent.setValue(builder.isOverMaxLevel() ? LevelRange.SHORT : LevelRange.BYTE);
    }

    @Override
    protected List<ScrollableButtonComponent> getCategories() {
        final String baseTranslationKey = "fzmm.gui.itemEditor.enchant.category.";
        List<ScrollableButtonComponent> buttonList = new ArrayList<>();

        buttonList.add(new ScrollableButtonComponent(Text.translatable("fzmm.gui.button.category.all"),
                buttonComponent -> this.applyCategory((enchantment, itemStack) -> true, buttonList, buttonComponent)));

        ScrollableButtonComponent applicableButton = new ScrollableButtonComponent(Text.translatable(baseTranslationKey + "applicable"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = Enchantment::isAcceptableItem;
            this.applyCategory(predicate, buttonList, buttonComponent);
        });
        buttonList.add(applicableButton);
        applicableButton.onPress(); // default category

        buttonList.add(new ScrollableButtonComponent(Text.translatable(baseTranslationKey + "curse"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = (enchantment, itemStack) -> enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(new ScrollableButtonComponent(Text.translatable(baseTranslationKey + "normal"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = (enchantment, itemStack) -> !enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        for (var target : EnchantmentTarget.values()) {
            String valueKey = target.name().toLowerCase();
            String translationKey = baseTranslationKey + "target." + valueKey;
            Text translation = Text.translatable(translationKey);

            // if there is no translation the value is used,
            // most likely mods that add enchantments will not have translation
            if (translation.getString().equals(translationKey))
                translation = Text.literal(valueKey);

            buttonList.add(new ScrollableButtonComponent(translation, buttonComponent -> {
                ILevelablePredicate<Enchantment> predicate = (enchantment, itemStack) -> enchantment.target == target;
                this.applyCategory(predicate, buttonList, buttonComponent);
            }));
        }

        this.addModsLevelablesToCategories(buttonList);

        for (var button : buttonList) {
            button.horizontalSizing(Sizing.fill(100));
            button.renderer(ButtonComponent.Renderer.flat(0x20000000, 0x40000000, 0x80000000));
        }

        return buttonList;
    }

    @Override
    protected AddLevelableComponent<Enchantment, EnchantmentBuilder.EnchantmentData, EnchantmentBuilder> getAddLevelableComponent(
            EnchantmentBuilder.EnchantmentData levelable, @Nullable Runnable callback) {
        return new AddEnchantComponent(levelable, callback, this, this.levelableBuilder);
    }

    @Override
    protected AppliedLevelableComponent<Enchantment, EnchantmentBuilder.EnchantmentData, EnchantmentBuilder> getAppliedLevelableComponent(EnchantmentBuilder.EnchantmentData levelable, @Nullable Runnable callback) {
        var value = super.getAppliedLevelableComponent(levelable, callback);
        LevelRange levelRange = (LevelRange) this.levelRangeEnumComponent.getValue();
        value.setLevelRange(levelRange.getMinLevel(), levelRange.getMaxLevel(levelable));
        return value;
    }

    @Override
    public Text getLevelableName(EnchantmentBuilder.EnchantmentData levelable) {
        Enchantment value = levelable.getValue();
        List<Text> textList = new ArrayList<>();
        for (var entry : this.getEntries()) {
            Enchantment enchantment = entry.getValue();
            if (enchantment != value && !value.canCombine(enchantment)) {
                textList.add(Text.translatable("fzmm.gui.itemEditor.enchant.option.incompatible.value", entry.getName()));
            }
        }

        MutableText labelText = levelable.getName().copy();
        if (!textList.isEmpty()) {
            labelText.append(Text.literal("*").setStyle(Style.EMPTY.withColor(0xE62600)));
            textList.add(0, Text.translatable("fzmm.gui.itemEditor.enchant.option.incompatible.tooltip"));
            MutableText tooltip = Text.empty();
            for (int i = 0; i < textList.size(); i++) {
                tooltip.append(textList.get(i));

                if (i != textList.size() - 1) {
                    tooltip.append(Text.literal("\n"));
                }
            }

            labelText.setStyle(labelText.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)));
        }

        return labelText;
    }

    @Override
    public Registry<Enchantment> getRegistry() {
        return Registries.ENCHANTMENT;
    }

    @Override
    public List<EnchantmentBuilder.EnchantmentData> getEntries() {
        Registry<Enchantment> registry = this.getRegistry();
        List<EnchantmentBuilder.EnchantmentData> enchantments = new ArrayList<>();

        for (var entry : registry.getEntrySet()) {
            enchantments.add(new EnchantmentBuilder.EnchantmentData(entry.getValue(), 1));
        }

        return enchantments;
    }

    @Override
    protected boolean disableFilter(EnchantmentBuilder.EnchantmentData levelable) {
        return this.onlyCompatibleEnchants && !this.levelableBuilder.isCompatibleWith(levelable.getValue());
    }

    public boolean isOnlyCompatibleEnchants() {
        return this.onlyCompatibleEnchants;
    }

    private enum LevelRange implements IMode {
        SHORT("short", 0, enchantmentData -> 32767),
        BYTE("byte", 0, enchantmentData -> 255),
        VANILLA("vanilla", 1, EnchantmentBuilder.EnchantmentData::getMaxLevel);

        private final String name;
        private final Function<EnchantmentBuilder.EnchantmentData, Integer> maxLevelFunc;
        private final int minLevel;

        LevelRange(String name, int minLevel, Function<EnchantmentBuilder.EnchantmentData, Integer> maxLevelFunc) {
            this.name = name;
            this.minLevel = minLevel;
            this.maxLevelFunc = maxLevelFunc;
        }

        @Override
        public String getTranslationKey() {
            return "fzmm.gui.itemEditor.effect.option.maxLevel." + this.name;
        }

        public int getMaxLevel(EnchantmentBuilder.EnchantmentData levelable) {
            return this.maxLevelFunc.apply(levelable);
        }

        public int getMinLevel() {
            return this.minLevel;
        }
    }
}
