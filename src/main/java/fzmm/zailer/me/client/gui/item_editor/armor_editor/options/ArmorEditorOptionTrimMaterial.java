package fzmm.zailer.me.client.gui.item_editor.armor_editor.options;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ArmorEditorOptionTrimMaterial extends AbstractArmorEditorOptionList<ArmorTrimMaterial> {
    public ArmorEditorOptionTrimMaterial(ArmorEditorScreen parent) {
        super(parent);
    }

    @Override
    public List<ArmorTrimMaterial> getValueList() {
        assert MinecraftClient.getInstance().world != null;
        Optional<Registry<ArmorTrimMaterial>> trimPatternRegistryOptional = MinecraftClient.getInstance().world.getRegistryManager().getOptional(RegistryKeys.TRIM_MATERIAL);
        return trimPatternRegistryOptional
                .map(armorTrimMaterials -> armorTrimMaterials.stream()
                        .sorted(Comparator.comparing(ArmorTrimMaterial::assetName))
                        .toList()
                ).orElseGet(ArrayList::new);
    }

    @Override
    public String getValueId(@Nullable ArmorTrimMaterial value) {
        return "trim-material-" + (value == null ? "empty" : value.assetName());
    }

    @Override
    public boolean isValueSelected(@Nullable ArmorTrimMaterial value) {
        return value == this.selectedArmorBuilder.trimMaterial();
    }

    @Override
    public Component getLayout(@Nullable ArmorTrimMaterial value, String id, boolean selected) {
        StackLayout stackLayout = this.parent.getButtonWithItemOver(this.getButtonItem(value), id, selected);

        ButtonComponent button = stackLayout.childById(ButtonComponent.class, id);
        BaseFzmmScreen.checkNull(button, "button", id);
        this.options.put(value, button);
        button.onPress(buttonComponent -> this.execute(value));

        return stackLayout;
    }

    private ItemStack getButtonItem(@Nullable ArmorTrimMaterial value) {
        if (value == null) {
            return DisplayBuilder.of(Items.BARRIER.getDefaultStack())
                    .setName(Text.translatable("fzmm.gui.itemEditor.armor.label.emptyTrim"))
                    .get();
        } else {
            return value.ingredient().value().getDefaultStack();
        }
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
    protected void selectOption(ArmorTrimMaterial value) {
        this.selectedArmorBuilder.trimMaterial(value);
    }

    @Override
    protected boolean addFirstValueNull() {
        return true;
    }
}
