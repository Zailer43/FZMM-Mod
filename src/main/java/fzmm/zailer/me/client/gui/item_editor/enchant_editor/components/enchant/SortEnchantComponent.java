package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;

import java.util.List;

public class SortEnchantComponent extends AbstractEnchantComponent implements IListEntry<AbstractEnchantComponent> {
    private ButtonComponent upButton;
    private ButtonComponent downButton;

    public SortEnchantComponent(Enchantment enchantment, int level, EnchantEditor editor, EnchantmentBuilder builder) {
        super(enchantment, level, editor, builder);
    }

    @Override
    protected List<ButtonComponent> getButtons(EnchantEditor editor, EnchantmentBuilder builder) {
        this.upButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.up"), button -> {})
                .horizontalSizing(Sizing.fixed(20));

        this.downButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.down"), button -> {})
                .horizontalSizing(Sizing.fixed(20));

        return List.of(this.upButton, this.downButton);
    }

    public ButtonComponent getUpButton() {
        return this.upButton;
    }

    public ButtonComponent getDownButton() {
        return this.downButton;
    }

    @Override
    public AbstractEnchantComponent getValue() {
        return new SortEnchantComponent(this.getEnchantment(), this.getLevel(), this.editor, this.builder);
    }

    @Override
    public void setValue(AbstractEnchantComponent value) {
        this.textBox.setText(value.getTextBox().getText());
        this.textBox.setCursorToStart(false);
        this.setDisabled(value.isDisabled);
        this.setButtonList(value.buttonList);
        this.setEnchantment(value.getEnchantment());
    }
}
