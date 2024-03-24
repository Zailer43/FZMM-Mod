package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AddLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import net.minecraft.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

public class AddEnchantComponent extends AddLevelableComponent<Enchantment, EnchantmentBuilder.EnchantmentData, EnchantmentBuilder> {

    public AddEnchantComponent(EnchantmentBuilder.EnchantmentData levelable, @Nullable Runnable callback,
                               EnchantEditor editor, EnchantmentBuilder builder) {
        super(levelable, callback, editor, builder);
    }

    @Override
    protected void onExecute() {
        if (((EnchantEditor) this.editor).isOnlyCompatibleEnchants()) {
            for (var child : this.editor.getAddLevelablesComponents()) {
                child.getLevelable().getValue()
                        .ifPresent(enchantment -> child.setDisabled(!this.builder.isCompatibleWith(enchantment)));
            }
        }
    }
}
