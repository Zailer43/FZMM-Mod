package fzmm.zailer.me.client.gui.item_editor.custom_model_data.components;

import fzmm.zailer.me.builders.CustomModelDataBuilder;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class CustomModelItemComponent extends ItemComponent {
    @Nullable
    private Integer modelValue;

    public CustomModelItemComponent() {
        super(ItemStack.EMPTY);
        this.sizing(Sizing.fixed(32));
    }

    public void clear() {
        this.modelValue = null;
        this.stack(ItemStack.EMPTY);
        this.cursorStyle(CursorStyle.POINTER);
        this.tooltip(new ArrayList<TooltipComponent>());
    }

    public void setModel(ItemStack stack, @Nullable Integer modelValue) {
        this.modelValue = modelValue;
        this.stack(CustomModelDataBuilder.builder().of(stack).value(modelValue).get());
        this.cursorStyle(CursorStyle.HAND);
        this.tooltip(Text.translatable(modelValue == null ? "fzmm.gui.itemEditor.custom_model_data.label.defaultValue"
                : "fzmm.gui.itemEditor.custom_model_data.label.value", modelValue)
        );
    }

    public Optional<Integer> getModel() {
        return Optional.ofNullable(this.modelValue);
    }
}
