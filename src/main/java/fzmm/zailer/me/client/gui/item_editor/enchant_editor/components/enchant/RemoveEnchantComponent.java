package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RemoveEnchantComponent extends AbstractEnchantComponent {
    private final Runnable callback;

    public RemoveEnchantComponent(Enchantment enchantment, int level, @Nullable Runnable callback,
                                  EnchantEditor editor, EnchantmentBuilder builder) {
        super(enchantment, level, editor, builder);
        this.callback = callback;
    }
    @Override
    protected List<ButtonComponent> getButtons(EnchantEditor editor, EnchantmentBuilder builder) {
        ButtonComponent removeButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.remove"), button -> {
            if (this.isDisabled)
                return;

            int index = this.getEnchantIndex(editor);

            if (index >= 0) {
                builder.remove(index);
                editor.updateItemPreview();

                editor.getAppliedEnchantsLayout().removeChild(this);

                if (this.callback != null)
                    this.callback.run();
            }
        }).horizontalSizing(Sizing.fixed(20));

        return List.of(removeButton);
    }

    @Override
    protected void setupComponent() {
        super.setupComponent();

        this.textBox.onChanged().subscribe(value -> {
            int parsedValue = this.getLevel();

            int index = this.getEnchantIndex(this.editor);

            if (index >= 0) {
                builder.setLevel(index, parsedValue);
                this.editor.updateItemPreview();
            }
        });
    }

    /**
     * @return the index of the enchantment in the list, -1 if not found
     */
    public int getEnchantIndex(EnchantEditor editor) {
        List<Component> children = editor.getAppliedEnchantsLayout().children();

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == this) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void setTextValue(String value) {
        super.setTextValue(value);
    }
}
