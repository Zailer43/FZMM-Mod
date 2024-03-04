package fzmm.zailer.me.client.gui.item_editor.enchant_editor;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AddLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.AddEnchantComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
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

public class EnchantEditor extends LevelableEditor<Enchantment, EnchantmentBuilder.EnchantmentData, EnchantmentBuilder> implements IItemEditorScreen {
    private boolean glint;
    private boolean onlyCompatibleEnchants;


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

        // top boolean buttons
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

        booleanLayout.child(glintComponent);
        booleanLayout.child(onlyCompatibleEnchantsComponent);

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
    }

    @Override
    protected List<ButtonComponent> addCategories() {
        final String baseTranslationKey = "fzmm.gui.itemEditor.enchant.category.";
        List<ButtonComponent> buttonList = new ArrayList<>();

        buttonList.add(Components.button(Text.translatable("fzmm.gui.button.category.all"),
                buttonComponent -> this.applyCategory((enchantment, itemStack) -> true, buttonList, buttonComponent)));

        ButtonComponent applicableButton = Components.button(Text.translatable(baseTranslationKey + "applicable"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = Enchantment::isAcceptableItem;
            this.applyCategory(predicate, buttonList, buttonComponent);
        });
        buttonList.add(applicableButton);
        applicableButton.onPress(); // default category

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "curse"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = (enchantment, itemStack) -> enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "normal"), buttonComponent -> {
            ILevelablePredicate<Enchantment> predicate = (enchantment, itemStack) -> !enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        for (var target : EnchantmentTarget.values()) {
            buttonList.add(Components.button(Text.translatable(baseTranslationKey + "target." + target.name().toLowerCase()), buttonComponent -> {
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

}
