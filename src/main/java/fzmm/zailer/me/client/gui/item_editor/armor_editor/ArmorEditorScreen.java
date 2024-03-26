package fzmm.zailer.me.client.gui.item_editor.armor_editor;

import fzmm.zailer.me.builders.ArmorBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.options.ArmorEditorOptionArmorMaterial;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.options.ArmorEditorOptionTrimMaterial;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.options.ArmorEditorOptionTrimPattern;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class ArmorEditorScreen implements IItemEditorScreen {
    private static final String ARMOR_PREVIEW_ID = "armor-preview";
    private static final String SELECT_PART_ID = "select-part";
    private static final String SELECT_ARMOR_MATERIAL_ID = "select-armor-material";
    private static final String SELECT_TRIM_MATERIAL_ID = "select-trim-material";
    private static final String SELECT_TRIM_PATTERN_ID = "select-trim-pattern";
    private final ArmorBuilder helmetBuilder = ArmorBuilder.builder();
    private final ArmorBuilder chestplateBuilder = ArmorBuilder.builder();
    private final ArmorBuilder leggingsBuilder = ArmorBuilder.builder();
    private final ArmorBuilder bootsBuilder = ArmorBuilder.builder();
    private final ArmorEditorOptionArmorMaterial armorMaterialOption = new ArmorEditorOptionArmorMaterial(this);
    private final ArmorEditorOptionTrimMaterial trimMaterialOption = new ArmorEditorOptionTrimMaterial(this);
    private final ArmorEditorOptionTrimPattern trimPatternOption = new ArmorEditorOptionTrimPattern(this);
    private ArmorStandEntity armorStandPreview;
    private List<RequestedItem> requestedItemList = null;
    private RequestedItem helmetRequest;
    private RequestedItem chestplateRequest;
    private RequestedItem leggingsRequest;
    private RequestedItem bootsRequest;
    private RequestedItem selectedArmorPart;
    private ArmorBuilder selectedArmorPartBuilder;
    private HashMap<Predicate<ItemStack>, ButtonComponent> armorPartButtons;
    private FlowLayout armorMaterialLayout;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItemList != null)
            return this.requestedItemList;

        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.world != null;
        PlayerInventory playerInventory = client.player.getInventory();
        List<SmithingRecipe> recipeTypes = client.world.getRecipeManager().listAllOfType(RecipeType.SMITHING).stream()
                .map(RecipeEntry::value).toList();

        this.helmetRequest = new RequestedItem(
                (itemStack) -> this.test(itemStack, EquipmentSlot.HEAD, recipeTypes),
                itemStack -> this.setSelectedItem(itemStack, this.helmetRequest, this.helmetBuilder),
                null,
                playerInventory.getArmorStack(PlayerInventory.ARMOR_SLOTS[3]),
                Text.translatable("fzmm.gui.itemEditor.armor.item.helmet"),
                false
        );

        this.chestplateRequest = new RequestedItem(
                (itemStack) -> this.test(itemStack, EquipmentSlot.CHEST, recipeTypes),
                itemStack -> this.setSelectedItem(itemStack, this.chestplateRequest, this.chestplateBuilder),
                null,
                playerInventory.getArmorStack(PlayerInventory.ARMOR_SLOTS[2]),
                Text.translatable("fzmm.gui.itemEditor.armor.item.chestplate"),
                false
        );

        this.leggingsRequest = new RequestedItem(
                (itemStack) -> this.test(itemStack, EquipmentSlot.LEGS, recipeTypes),
                itemStack -> this.setSelectedItem(itemStack, this.leggingsRequest, this.leggingsBuilder),
                null,
                playerInventory.getArmorStack(PlayerInventory.ARMOR_SLOTS[1]),
                Text.translatable("fzmm.gui.itemEditor.armor.item.leggings"),
                false
        );

        this.bootsRequest = new RequestedItem(
                (itemStack) -> this.test(itemStack, EquipmentSlot.FEET, recipeTypes),
                itemStack -> this.setSelectedItem(itemStack, this.bootsRequest, this.bootsBuilder),
                null,
                playerInventory.getArmorStack(PlayerInventory.ARMOR_SLOTS[0]),
                Text.translatable("fzmm.gui.itemEditor.armor.item.boots"),
                false
        );

        this.requestedItemList = List.of(this.helmetRequest, this.chestplateRequest, this.leggingsRequest, this.bootsRequest);
        return this.requestedItemList;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.DIAMOND_HELMET.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        EntityComponent<?> armorPreviewComponent = editorLayout.childById(EntityComponent.class, ARMOR_PREVIEW_ID);
        BaseFzmmScreen.checkNull(armorPreviewComponent, "entity", ARMOR_PREVIEW_ID);
        this.armorStandPreview = (ArmorStandEntity) armorPreviewComponent.entity();
        this.armorStandPreview.setHideBasePlate(true);

        this.selectedArmorPart = this.helmetRequest;
        this.selectedArmorPartBuilder = this.helmetBuilder;

        this.armorPartButtons = new HashMap<>();
        FlowLayout selectPartLayout = editorLayout.childById(FlowLayout.class, SELECT_PART_ID);
        BaseFzmmScreen.checkNull(selectPartLayout, "flow-layout", SELECT_PART_ID);
        List<Component> selectPartChildren = new ArrayList<>();
        selectPartChildren.add(this.getPartComponent(Items.DIAMOND_HELMET, helmetRequest, helmetBuilder));
        selectPartChildren.add(this.getPartComponent(Items.DIAMOND_CHESTPLATE, chestplateRequest, chestplateBuilder));
        selectPartChildren.add(this.getPartComponent(Items.DIAMOND_LEGGINGS, leggingsRequest, leggingsBuilder));
        selectPartChildren.add(this.getPartComponent(Items.DIAMOND_BOOTS, bootsRequest, bootsBuilder));
        selectPartLayout.children(selectPartChildren);

        this.armorMaterialLayout = editorLayout.childById(FlowLayout.class, SELECT_ARMOR_MATERIAL_ID);
        BaseFzmmScreen.checkNull(this.armorMaterialLayout, "flow-layout", SELECT_ARMOR_MATERIAL_ID);

        FlowLayout trimMaterialLayout = editorLayout.childById(FlowLayout.class, SELECT_TRIM_MATERIAL_ID);
        BaseFzmmScreen.checkNull(trimMaterialLayout, "flow-layout", SELECT_TRIM_MATERIAL_ID);

        FlowLayout trimPatternLayout = editorLayout.childById(FlowLayout.class, SELECT_TRIM_PATTERN_ID);
        BaseFzmmScreen.checkNull(trimPatternLayout, "flow-layout", SELECT_TRIM_PATTERN_ID);

        this.updateSelectedArmorReference();
        this.updateSelectedOptions();

        this.armorMaterialOption.generateLayout(this.armorMaterialLayout);
        this.trimMaterialOption.generateLayout(trimMaterialLayout);
        this.trimPatternOption.generateLayout(trimPatternLayout);

        this.armorMaterialOption.setExecuteCallback(this::update);
        this.trimMaterialOption.setExecuteCallback(this::update);
        this.trimPatternOption.setExecuteCallback(this::update);

        return editorLayout;
    }

    @Override
    public String getId() {
        return "armor";
    }

    @Override
    public void updateItemPreview() {
        this.selectedArmorPart.setStack(this.selectedArmorPartBuilder.get());
        this.selectedArmorPart.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        // in case the user, for reasons that science cannot explain,
        // decided to put another piece of armor on the head, otherwise
        // it will be added to the corresponding slot and to the head slot.
        for (var requestedItem : this.requestedItemList) {
            if (requestedItem.stack() == stack)
                return;
        }

        if (this.bootsRequest.predicate().test(stack))
            this.setSelectedItem(stack, this.bootsRequest, this.bootsBuilder);
        else if (this.leggingsRequest.predicate().test(stack))
            this.setSelectedItem(stack, this.leggingsRequest, this.leggingsBuilder);
        else if (this.chestplateRequest.predicate().test(stack))
            this.setSelectedItem(stack, this.chestplateRequest, this.chestplateBuilder);
        else
            this.setSelectedItem(stack, this.helmetRequest, this.helmetBuilder);
    }

    private void setSelectedItem(ItemStack stack, RequestedItem requestedItem, ArmorBuilder builder) {
        this.selectedArmorPart = requestedItem;
        this.selectedArmorPartBuilder = builder;
        builder.of(stack);
        this.selectArmorPartExecute(this.selectedArmorPart, this.selectedArmorPartBuilder);
        this.update();
    }

    public boolean test(ItemStack itemStack, EquipmentSlot slot, List<SmithingRecipe> recipeTypes) {
        return itemStack.getItem() instanceof Equipment equipment &&
                equipment.getSlotType() == slot &&
                recipeTypes.stream().anyMatch(smithingRecipe -> smithingRecipe.testBase(itemStack));
    }


    private Component getPartComponent(Item item, RequestedItem requestedItem, ArmorBuilder builder) {
        String id = "part-" + item.toString();
        StackLayout stackLayout = this.getButtonWithItemOver(item.getDefaultStack(), id, requestedItem.predicate().test(item.getDefaultStack()));

        ButtonComponent button = stackLayout.childById(ButtonComponent.class, id);
        BaseFzmmScreen.checkNull(button, "button", id);
        this.armorPartButtons.put(requestedItem.predicate(), button);
        button.onPress(buttonComponent -> {
            this.selectArmorPartExecute(requestedItem, builder);
            this.update();
        });
        return stackLayout;
    }

    public StackLayout getButtonWithItemOver(ItemStack stack, String buttonId, boolean active) {
        Component itemComponent = Components.item(stack)
                .setTooltipFromStack(true)
                .sizing(Sizing.fixed(16), Sizing.fixed(16))
                .margins(Insets.of(2))
                .cursorStyle(CursorStyle.HAND);

        ButtonComponent buttonComponent = Components.button(Text.empty(), button -> {
        });
        buttonComponent.sizing(Sizing.fixed(20), Sizing.fixed(20))
                .margins(Insets.bottom(4))
                .id(buttonId);
        buttonComponent.active = !active;

        return Containers.stack(Sizing.content(), Sizing.content())
                .child(buttonComponent)
                .child(itemComponent);
    }

    public void updateSelectedArmorReference() {
        this.armorMaterialOption.setSelectedArmor(this.selectedArmorPartBuilder, this.selectedArmorPart);
        this.trimMaterialOption.setSelectedArmor(this.selectedArmorPartBuilder, this.selectedArmorPart);
        this.trimPatternOption.setSelectedArmor(this.selectedArmorPartBuilder, this.selectedArmorPart);
    }

    public void updateSelectedOptions() {
        this.armorMaterialOption.updateSelectedOption(this.selectedArmorPartBuilder.item());
        this.trimMaterialOption.updateSelectedOption(this.selectedArmorPartBuilder.trimMaterial());
        this.trimPatternOption.updateSelectedOption(this.selectedArmorPartBuilder.trimPattern());
    }

    public void update() {
        this.updateItemPreview();

        this.toggleArmorPartButtons(this.selectedArmorPart);
        this.updateSelectedOptions();

        this.trimPatternOption.updatePreview();
        this.updateArmorStandArmor(this.armorStandPreview);
    }

    private void selectArmorPartExecute(RequestedItem selectedPart, ArmorBuilder builder) {
        this.selectedArmorPart = selectedPart;
        this.selectedArmorPartBuilder = builder;
        this.updateSelectedArmorReference();
        this.armorMaterialOption.generateLayout(this.armorMaterialLayout);
    }

    private void toggleArmorPartButtons(RequestedItem requestedItem) {
        for (var button : this.armorPartButtons.values())
            button.active = true;

        ButtonComponent button = this.armorPartButtons.get(requestedItem.predicate());
        if (button != null)
            button.active = false;
    }

    public ArmorBuilder getBootsBuilder() {
        return this.bootsBuilder;
    }

    public ArmorBuilder getLeggingsBuilder() {
        return this.leggingsBuilder;
    }

    public ArmorBuilder getChestplateBuilder() {
        return this.chestplateBuilder;
    }

    public ArmorBuilder getHelmetBuilder() {
        return this.helmetBuilder;
    }

    public void updateArmorStandArmor(ArmorStandEntity entity) {
        entity.equipStack(EquipmentSlot.HEAD, this.helmetRequest.stack());
        entity.equipStack(EquipmentSlot.CHEST, this.chestplateRequest.stack());
        entity.equipStack(EquipmentSlot.LEGS, this.leggingsRequest.stack());
        entity.equipStack(EquipmentSlot.FEET, this.bootsRequest.stack());
    }

}
