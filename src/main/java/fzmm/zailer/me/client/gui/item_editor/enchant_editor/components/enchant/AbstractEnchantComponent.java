package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractEnchantComponent extends FlowLayout {
    private Enchantment enchantment;
    protected final LabelComponent label;
    protected final ConfigTextBox textBox;
    protected String labelValue;
    protected List<ButtonComponent> buttonList;
    protected boolean isDisabled;
    protected EnchantEditor editor;
    protected EnchantmentBuilder builder;

    public AbstractEnchantComponent(Enchantment enchantment, int level, EnchantEditor editor, EnchantmentBuilder builder) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);

        this.label = (LabelComponent) Components.label(Text.empty()).horizontalSizing(Sizing.fixed(editor.getEnchantsLabelHorizontalSize()));
        this.setEnchantment(enchantment);

        ConfigTextBox textBox = new ConfigTextBox().configureForNumber(Short.class);
        textBox.setText(String.valueOf(level));
        textBox.horizontalSizing(Sizing.fixed(40));
        textBox.setCursorToStart(false);
        this.textBox = textBox;

        this.isDisabled = false;
        this.editor = editor;
        this.builder = builder;
        this.setupComponent();
    }

    protected void setupComponent() {
        FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(20));

        layout.child(this.textBox);

        this.buttonList = this.getButtons(this.editor, this.builder);
        this.children(this.buttonList);

        layout.child(this.label);

        layout.gap(2);
        layout.verticalAlignment(VerticalAlignment.CENTER);
        this.child(layout);
    }

    protected abstract List<ButtonComponent> getButtons(EnchantEditor editor, EnchantmentBuilder builder);

    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    public int getLevel() {
        return (short) this.textBox.parsedValue();
    }

    public ConfigTextBox getTextBox() {
        return this.textBox;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;

        List<Text> textList = new ArrayList<>();
        for (var entry : Registries.ENCHANTMENT.getEntrySet()) {
            Enchantment enchant = entry.getValue();
            if (enchant != enchantment && !this.enchantment.canCombine(enchant)) {
                textList.add(Text.translatable("fzmm.gui.itemEditor.enchant.option.incompatible.value", this.getEnchantName(enchant)));
            }
        }

        Text enchantName = enchantment.getName(1);
        MutableText labelText = enchantName.copyContentOnly().setStyle(enchantName.getStyle()); // remove enchantment level of enchant name
        this.label.text(labelText);
        this.labelValue = labelText.getString().toLowerCase();
        if (!textList.isEmpty()) {
            labelText.append(Text.literal("*").setStyle(Style.EMPTY.withColor(0xE62600)));
            textList.add(0, Text.translatable("fzmm.gui.itemEditor.enchant.option.incompatible.tooltip"));
            this.label.tooltip(textList);
        }
    }

    public void setTextValue(String value) {
        this.textBox.text(value);
        this.textBox.setCursorToStart(false);
    }

    protected MutableText getEnchantName(Enchantment enchantment) {
        Text enchantName = enchantment.getName(1);
        return enchantName.copyContentOnly().setStyle(enchantName.getStyle()); // remove enchantment level of enchant name
    }

    public boolean filter(String value) {
        return value.isEmpty() || this.labelValue.contains(value.toLowerCase());
    }

    public void setDisabled(boolean value) {

        if (value == this.isDisabled)
            return;

        this.isDisabled = value;
        for (var button : this.buttonList) {
            button.active = !value;
        }

        Style style = this.label.text().getStyle().withStrikethrough(value);
        Text text = this.label.text().copy().setStyle(style);
        this.label.text(text);
    }

    protected void setButtonList(List<ButtonComponent> buttonList) {
        this.buttonList = buttonList;
    }
}
