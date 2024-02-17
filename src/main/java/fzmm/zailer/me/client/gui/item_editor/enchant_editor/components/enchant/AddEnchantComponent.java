package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AddEnchantComponent extends AbstractEnchantComponent {
    private ButtonComponent button;
    @Nullable
    private final Runnable callback;
    private boolean useCallback;

    public AddEnchantComponent(Enchantment enchantment, int level, @Nullable Runnable callback,
                               EnchantEditor editor, EnchantmentBuilder builder) {
        super(enchantment, level, editor, builder);
        this.callback = callback;
        this.useCallback = true;
    }
    @Override
    protected List<ButtonComponent> getButtons(EnchantEditor editor, EnchantmentBuilder builder) {
        this.button = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.add"), button -> {
            if (this.isDisabled)
                return;
            builder.add(this.getEnchantment(), this.getLevel());
            editor.updateItemPreview();

            if (!editor.isAllowDuplicates()) {
                editor.getAddEnchantsLayout().removeChild(this);
            }

            if (editor.isOnlyCompatibleEnchants()) {
                for (var child : editor.getAddEnchantsComponents()) {
                    child.setDisabled(!builder.isCompatibleWith(child.getEnchantment()));
                }
            }

            if (this.callback != null && this.useCallback)
                this.callback.run();
        }).horizontalSizing(Sizing.fixed(20));

        return List.of(this.button);
    }

    public void addExecute() {
        this.useCallback = false;
        this.button.onPress();
        this.useCallback = true;
        if (this.callback != null)
            this.callback.run();
    }
}
