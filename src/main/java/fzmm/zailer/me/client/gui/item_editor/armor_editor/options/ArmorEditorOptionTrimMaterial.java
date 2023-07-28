package fzmm.zailer.me.client.gui.item_editor.armor_editor.options;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

import java.util.Comparator;
import java.util.List;

public class ArmorEditorOptionTrimMaterial extends AbstractArmorEditorOptionList<ArmorTrimMaterial> {
    public ArmorEditorOptionTrimMaterial(ArmorEditorScreen parent) {
        super(parent);
    }

    @Override
    public List<ArmorTrimMaterial> getValueList() {
        try {
            assert MinecraftClient.getInstance().world != null;
            Registry<ArmorTrimMaterial> trimPatternRegistry = MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
            return trimPatternRegistry.stream().sorted(Comparator.comparing(ArmorTrimMaterial::assetName)).toList();
        } catch (IllegalStateException e) {
            FzmmClient.LOGGER.warn("[ArmorEditorOptionTrimMaterial] Failed to get value list, registry TRIM_MATERIAL not exists");
            return List.of();
        }
    }

    @Override
    public String getValueId(@Nullable ArmorTrimMaterial value) {
        return "trim-material-" + (value == null ? "empty" : value.assetName());
    }

    @Override
    public ArmorTrimMaterial getSelectedValue() {
        return this.selectedArmorBuilder.trimMaterial();
    }

    @Override
    public Component getLayout(@Nullable ArmorTrimMaterial value, String id) {
        StackLayout stackLayout = this.parent.getButtonWithItemOver((value == null ? Items.BARRIER : value.ingredient().value()).getDefaultStack(), id);

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
    protected void selectOption(ArmorTrimMaterial value) {
        this.selectedArmorBuilder.trimMaterial(value);
    }

    @Override
    protected boolean addFirstValueNull() {
        return true;
    }
}
