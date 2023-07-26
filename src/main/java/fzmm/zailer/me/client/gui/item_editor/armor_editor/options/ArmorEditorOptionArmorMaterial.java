package fzmm.zailer.me.client.gui.item_editor.armor_editor.options;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ArmorEditorOptionArmorMaterial extends AbstractArmorEditorOptionList<Item> {

    public ArmorEditorOptionArmorMaterial(ArmorEditorScreen parent) {
        super(parent);
    }

    @Override
    public List<Item> getValueList() {
        return this.selectedArmorRequest.defaultItems().stream().map(ItemStack::getItem).toList();
    }

    @Override
    public String getValueId(@Nullable Item value) {
        return "armor-material-" + (value == null ? "empty" : value.toString());
    }

    @Override
    public boolean isValueSelected(@Nullable Item value) {
        return value == this.selectedArmorBuilder.item();
    }

    @Override
    public Component getLayout(@Nullable Item value, String id, boolean selected) {
        StackLayout stackLayout = this.parent.getButtonWithItemOver(value.getDefaultStack(), id, selected);

        ButtonComponent button = stackLayout.childById(ButtonComponent.class, id);
        BaseFzmmScreen.checkNull(button, "button", id);
        this.options.put(value, button);
        button.onPress(buttonComponent -> this.execute(value));

        return stackLayout;
    }

    @Override
    protected void selectComponent(Component component) {
        if (component instanceof ButtonComponent button)
            button.active = false;
    }

    @Override
    protected void unselectComponent(Component component) {
        if (component instanceof ButtonComponent button)
            button.active = true;
    }

    @Override
    protected void selectOption(Item value) {
        this.selectedArmorBuilder.item(value);
    }

    @Override
    protected boolean addFirstValueNull() {
        return false;
    }
}
