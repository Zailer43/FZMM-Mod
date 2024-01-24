package fzmm.zailer.me.client.gui.item_editor.armor_editor.options;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.builders.ArmorBuilder;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ArmorEditorOptionTrimPattern extends AbstractArmorEditorOptionList<ArmorTrimPattern> {
    private final List<ArmorStandEntity> armorStandList = new ArrayList<>();

    public ArmorEditorOptionTrimPattern(ArmorEditorScreen parent) {
        super(parent);
    }

    @Override
    public void generateLayout(FlowLayout optionParent) {
        this.armorStandList.clear();
        super.generateLayout(optionParent);
        this.updatePreview();
    }

    @Override
    public List<ArmorTrimPattern> getValueList() {
        assert MinecraftClient.getInstance().world != null;
        Optional<Registry<ArmorTrimPattern>> trimPatternRegistryOptional = MinecraftClient.getInstance().world.getRegistryManager().getOptional(RegistryKeys.TRIM_PATTERN);
        return trimPatternRegistryOptional
                .map(armorTrimPatterns -> armorTrimPatterns.stream()
                        .sorted(Comparator.comparing(o -> o.assetId().getPath()))
                        .toList()
                ).orElseGet(ArrayList::new);
    }

    @Override
    public String getValueId(@Nullable ArmorTrimPattern value) {
        return "trim-pattern-" + (value == null ? "empty" : value.assetId().getPath());
    }

    @Override
    public boolean isValueSelected(@Nullable ArmorTrimPattern value) {
        return value == this.selectedArmorBuilder.trimPattern();
    }

    @Override
    public Component getLayout(@Nullable ArmorTrimPattern value, String id, boolean selected) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client != null;

        FlowLayout layout = Containers.horizontalFlow(Sizing.fixed(40), Sizing.fixed(75));
        layout.id(id);
        layout.tooltip(value == null ? Text.translatable("fzmm.gui.itemEditor.armor.label.emptyTrim") : value.description());

        layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        if (selected)
            layout.surface(Surface.flat(0x40000000));

        ArmorStandEntity armorStandEntity = new ArmorStandEntity(EntityType.ARMOR_STAND, client.world);
        this.parent.updateArmorStandArmor(armorStandEntity);
        armorStandEntity.setHideBasePlate(true);
        this.armorStandList.add(armorStandEntity);

        EntityComponent<ArmorStandEntity> entityComponent = Components.entity(Sizing.fixed(25), armorStandEntity);

        layout.child(entityComponent);

        layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.execute(value);

            return true;
        });

        this.options.put(value, layout);
        return layout;
    }

    @Override
    protected void selectComponent(Component component) {
        if (component instanceof FlowLayout layout)
            layout.surface(Surface.flat(0x60000000));
    }

    @Override
    protected void unselectComponent(Component component) {
        if (component instanceof FlowLayout layout)
            layout.surface(Surface.flat(0));
    }

    @Override
    protected void selectOption(ArmorTrimPattern value) {
        this.selectedArmorBuilder.trimPattern(value);
    }

    public void updatePreview() {
        assert MinecraftClient.getInstance().world != null;
        List<ArmorTrimPattern> trimPatterns = this.getValueList();

        for (int i = 0; i != this.armorStandList.size(); i++) {
            ArmorStandEntity armorStandEntity = this.armorStandList.get(i);
            ArmorTrimPattern trimPattern = null;
            if (i != 0)
                trimPattern = trimPatterns.get(i - 1);

            ArmorBuilder builder = this.selectedArmorBuilder.copy().trimPattern(trimPattern);
            ItemStack stack = builder.get();

            this.parent.updateArmorStandArmor(armorStandEntity);
            this.equipInArmorStand(armorStandEntity, stack);
        }
    }

    // Because otherwise if for X or Z reason the player tries to put an item that is equipped elsewhere,
    // the preview will appear with another item and the armor stands with patterns will be with the part
    // equipped where it is not but only visually, for example trying to put the elytras on your feet would
    // make the preview have the feet without anything but the previews of trim patterns would have it well-equipped
    // but in reality it would not let you modify it
    private void equipInArmorStand(ArmorStandEntity armorStandEntity, ItemStack stack) {
        if (this.selectedArmorBuilder == this.parent.getBootsBuilder())
            armorStandEntity.equipStack(EquipmentSlot.FEET, stack);
        else if (this.selectedArmorBuilder == this.parent.getLeggingsBuilder())
            armorStandEntity.equipStack(EquipmentSlot.LEGS, stack);
        else if (this.selectedArmorBuilder == this.parent.getChestplateBuilder())
            armorStandEntity.equipStack(EquipmentSlot.CHEST, stack);
        else if (this.selectedArmorBuilder == this.parent.getHelmetBuilder())
            armorStandEntity.equipStack(EquipmentSlot.HEAD, stack);
    }

    @Override
    protected boolean addFirstValueNull() {
        return true;
    }
}
