package fzmm.zailer.me.client.gui.item_editor.enchant_editor;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant.AbstractEnchantComponent;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant.AddEnchantComponent;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.EnchantSortOverlay;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant.RemoveEnchantComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class EnchantEditor implements IItemEditorScreen {
    private RequestedItem itemRequested = null;
    private List<RequestedItem> requestedItems = null;
    private FlowLayout appliedEnchantsLayout;
    private FlowLayout categoriesLayout;
    private TextBoxComponent searchTextBox;
    private ConfigTextBox setLevelsTextBox;
    private FlowLayout addEnchantsLayout;
    private ImmutableList<AddEnchantComponent> addEnchantsComponents;
    private List<RemoveEnchantComponent> appliedEnchantsComponents;
    private boolean glint;
    private boolean allowDuplicates;
    private boolean ignoreMaxLevel;
    private boolean onlyCompatibleEnchants;
    private int enchantsLabelHorizontalSize;
    private ButtonComponent selectedCategoryButton;
    private ButtonComponent sortButton;
    private final EnchantmentBuilder enchantBuilder = EnchantmentBuilder.builder();

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.itemRequested = new RequestedItem(
                itemStack -> {
                    for (var entry : Registries.ENCHANTMENT.getEntrySet()) {
                        if (entry.getValue().isAcceptableItem(itemStack))
                            return true;
                    }

                    return false;
                },
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.label.anyItem"),
                true
        );

        this.requestedItems = List.of(this.itemRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.ENCHANTED_BOOK.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, int x, int y, int width, int height) {
        UIModel uiModel = BaseUIModelScreen.DataSource.asset(new Identifier(FzmmClient.MOD_ID, "item_editor/enchant_editor")).get();
        if (uiModel == null) {
            FzmmClient.LOGGER.error("[EnchantEditor] Failed to load UIModel");
            return null;
        }

        assert MinecraftClient.getInstance().world != null;
        FlowLayout rootComponent = uiModel.createAdapterWithoutScreen(x, y, width, height, FlowLayout.class).rootComponent;

        this.appliedEnchantsComponents = new ArrayList<>();
        this.glint = false;
        this.allowDuplicates = false;
        this.ignoreMaxLevel = false;
        this.onlyCompatibleEnchants = false;
        this.enchantsLabelHorizontalSize = 0;

        // top boolean buttons
        ButtonComponent glintComponent = rootComponent.childById(ButtonComponent.class, "glint");
        BaseFzmmScreen.checkNull(glintComponent, "button", "glint");
        glintComponent.onPress(buttonComponent -> {
            this.glint = !this.glint;
            this.enchantBuilder.glint(this.glint);
            this.updateItemPreview();
        });
        glintComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.glint"));
        glintComponent.horizontalSizing(Sizing.fixed(20));
        this.setRenderButton(glintComponent, () -> this.glint, 48);

        ButtonComponent allowDuplicatesComponent = rootComponent.childById(ButtonComponent.class, "allow-duplicates");
        BaseFzmmScreen.checkNull(allowDuplicatesComponent, "button", "allow-duplicates");
        allowDuplicatesComponent.onPress(buttonComponent -> {
            this.allowDuplicates = !this.allowDuplicates;
            this.enchantBuilder.allowDuplicates(this.allowDuplicates);
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });
        allowDuplicatesComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.allowDuplicates"));
        allowDuplicatesComponent.horizontalSizing(Sizing.fixed(20));
        this.setRenderButton(allowDuplicatesComponent, () -> this.allowDuplicates, 80);

        ButtonComponent ignoreMaxLevelComponent = rootComponent.childById(ButtonComponent.class, "ignore-max-level");
        BaseFzmmScreen.checkNull(ignoreMaxLevelComponent, "button", "ignore-max-level");
        ignoreMaxLevelComponent.onPress(buttonComponent -> {
            this.ignoreMaxLevel = !this.ignoreMaxLevel;
            this.setLevelsExecute(this.ignoreMaxLevel);
        });
        ignoreMaxLevelComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.ignoreMaxLevel"));
        ignoreMaxLevelComponent.horizontalSizing(Sizing.fixed(20));
        this.setRenderButton(ignoreMaxLevelComponent, () -> this.ignoreMaxLevel, 112);

        ButtonComponent onlyCompatibleEnchantsComponent = rootComponent.childById(ButtonComponent.class, "only-compatible-enchants");
        BaseFzmmScreen.checkNull(onlyCompatibleEnchantsComponent, "button", "only-compatible-enchants");
        onlyCompatibleEnchantsComponent.onPress(buttonComponent -> {
            this.onlyCompatibleEnchants = !this.onlyCompatibleEnchants;
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });
        onlyCompatibleEnchantsComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.enchant.option.onlyCompatibleEnchants"));
        onlyCompatibleEnchantsComponent.horizontalSizing(Sizing.fixed(20));
        this.setRenderButton(onlyCompatibleEnchantsComponent, () -> this.onlyCompatibleEnchants, 144);

        // other top buttons
        Icon sortIcon = Icon.of(Items.HOPPER);
        this.sortButton = rootComponent.childById(ButtonComponent.class, "sort-enchants");
        BaseFzmmScreen.checkNull(this.sortButton, "button", "sort-enchants");
        this.sortButton.onPress(buttonComponent -> this.addSortOverlay((FlowLayout) rootComponent.root()));
        this.sortButton.horizontalSizing(Sizing.fixed(20));
        this.sortButton.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            sortIcon.render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });


        // right top button
        ButtonComponent removeAllButton = rootComponent.childById(ButtonComponent.class, "remove-all");
        BaseFzmmScreen.checkNull(removeAllButton, "button", "remove-all");
        removeAllButton.onPress(buttonComponent -> {
            this.enchantBuilder.removeAll();
            this.appliedEnchantsLayout.clearChildren();
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });

        // filters
        this.searchTextBox = rootComponent.childById(TextBoxComponent.class, "search");
        BaseFzmmScreen.checkNull(this.searchTextBox, "text-box", "search");
        this.searchTextBox.onChanged().subscribe(value -> {
            this.selectedCategoryButton.onPress();
            this.updateAppliedEnchants();
        });

        this.setLevelsTextBox = rootComponent.childById(ConfigTextBox.class, "set-levels");
        BaseFzmmScreen.checkNull(this.setLevelsTextBox, "text-box", "set-levels");
        this.setLevelsTextBox.configureForNumber(Integer.class);
        this.setLevelsTextBox.setText("1");
        this.setLevelsTextBox.setCursorToStart(false);
        this.setLevelsTextBox.applyPredicate(s -> {
            try {
                int value = Integer.parseInt(s);
                return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.setLevelsTextBox.onChanged().subscribe(value -> this.setLevelsExecute(this.ignoreMaxLevel));

        // content
        this.appliedEnchantsLayout = rootComponent.childById(FlowLayout.class, "applied-enchants");
        BaseFzmmScreen.checkNull(this.appliedEnchantsLayout, "flowLayout", "applied-enchants");

        // add all
        ButtonComponent addAllButton = rootComponent.childById(ButtonComponent.class, "add-all");
        BaseFzmmScreen.checkNull(addAllButton, "button", "add-all");
        addAllButton.onPress(buttonComponent -> {
            List<Component> children = this.addEnchantsLayout.children();
            for (var enchantComponent : List.copyOf(this.addEnchantsLayout.children())) {
                if (children.contains(enchantComponent))
                    ((AddEnchantComponent) enchantComponent).addExecute();
            }

            this.updateItemPreview();
        });

        // categories
        this.categoriesLayout = rootComponent.childById(FlowLayout.class, "categories");
        BaseFzmmScreen.checkNull(this.categoriesLayout, "flowLayout", "categories");

        // add enchantments layout
        this.addEnchantsLayout = rootComponent.childById(FlowLayout.class, "add-enchantments-layout");
        BaseFzmmScreen.checkNull(this.addEnchantsLayout, "flowLayout", "add-enchantments-layout");

        List<Enchantment> enchantments = this.getSortedEnchantments();

        this.setLabelSize(enchantments);
        this.setupAddEnchants(enchantments);
        this.updateAppliedEnchants();
        this.addCategories();

        return rootComponent;
    }

    private void setLevelsExecute(boolean ignoreMaxLevel) {
        int level = (int) this.setLevelsTextBox.parsedValue();
        level = MathHelper.clamp(level, Short.MIN_VALUE, Short.MAX_VALUE);
        if (!ignoreMaxLevel)
            level = EnchantmentBuilder.getMaxLevel(level);

        String valueStr = String.valueOf(level);
        for (var child : this.addEnchantsLayout.children())
            ((AbstractEnchantComponent) child).setTextValue(valueStr);

        for (var child : this.appliedEnchantsLayout.children()) {

            ((AbstractEnchantComponent) child).setTextValue(valueStr);
        }
    }

    @Override
    public String getId() {
        return "enchant";
    }

    @Override
    public void updateItemPreview() {
        this.itemRequested.setStack(this.enchantBuilder.get());
        this.itemRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.enchantBuilder.stack(stack);

        this.glint = this.enchantBuilder.glint();
        this.allowDuplicates = this.enchantBuilder.allowDuplicates();
        this.ignoreMaxLevel = this.enchantBuilder.isOverMaxLevel();
        this.onlyCompatibleEnchants = this.enchantBuilder.onlyCompatibleEnchants();

        this.selectedCategoryButton.onPress();
        this.updateAppliedEnchants();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    private void setLabelSize(List<Enchantment> enchantments) {
        int labelWidth = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        for (var enchant : enchantments) {
            Text name = Text.translatable(enchant.getTranslationKey()).append("*");
            labelWidth = Math.max(labelWidth, textRenderer.getWidth(name));
        }

        this.enchantsLabelHorizontalSize = labelWidth;
    }

    private void setupAddEnchants(List<Enchantment> enchantments) {
        ImmutableList.Builder<AddEnchantComponent> addEnchantComponentBuilder = ImmutableList.builder();
        for (var enchant : enchantments) {
            AddEnchantComponent enchantComponent = new AddEnchantComponent(enchant, (int) this.setLevelsTextBox.parsedValue(),
                    this::updateAppliedEnchants, this, this.enchantBuilder
            );
            addEnchantComponentBuilder.add(enchantComponent);
        }
        this.addEnchantsComponents = addEnchantComponentBuilder.build();
    }

    private void addCategories() {
        final String baseTranslationKey = "fzmm.gui.itemEditor.enchant.category.";
        List<ButtonComponent> buttonList = new ArrayList<>();

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "all"),
                buttonComponent -> this.applyCategory((enchantment, itemStack) -> true, buttonList, buttonComponent)));

        ButtonComponent applicableButton = Components.button(Text.translatable(baseTranslationKey + "applicable"), buttonComponent -> {
            IEnchantPredicate predicate = Enchantment::isAcceptableItem;
            this.applyCategory(predicate, buttonList, buttonComponent);
        });
        buttonList.add(applicableButton);
        applicableButton.onPress(); // default category

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "curse"), buttonComponent -> {
            IEnchantPredicate predicate = (enchantment, itemStack) -> enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        buttonList.add(Components.button(Text.translatable(baseTranslationKey + "normal"), buttonComponent -> {
            IEnchantPredicate predicate = (enchantment, itemStack) -> !enchantment.isCursed();
            this.applyCategory(predicate, buttonList, buttonComponent);
        }));

        for (var target : EnchantmentTarget.values()) {
            buttonList.add(Components.button(Text.translatable(baseTranslationKey + "target." + target.name().toLowerCase()), buttonComponent -> {
                IEnchantPredicate predicate = (enchantment, itemStack) -> enchantment.target == target;
                this.applyCategory(predicate, buttonList, buttonComponent);
            }));
        }

        Set<String> enchantsMods = new HashSet<>();
        for (var identifier : Registries.ENCHANTMENT.getIds()) {
            enchantsMods.add(identifier.getNamespace());
        }

        for (var modId : enchantsMods) {
            Text translation = modId.equals("minecraft") ?
                    Text.translatable(baseTranslationKey + "vanilla") :
                    Text.translatable(baseTranslationKey + "mod", modId);

            buttonList.add(Components.button(translation, buttonComponent -> {
                IEnchantPredicate predicate = (enchantment, itemStack) -> {
                    Identifier enchantmentId = Registries.ENCHANTMENT.getId(enchantment);
                    return enchantmentId != null && modId.equals(enchantmentId.getNamespace());
                };
                this.applyCategory(predicate, buttonList, buttonComponent);
            }));
        }

        for (var button : buttonList) {
            button.horizontalSizing(Sizing.fill(100));
            button.renderer(ButtonComponent.Renderer.flat(0x20000000, 0x40000000, 0x80000000));
        }

        this.categoriesLayout.children(buttonList);
    }

    private void applyCategory(IEnchantPredicate stackPredicate, List<ButtonComponent> buttonList, ButtonComponent button) {
        for (var entry : buttonList)
            entry.active = true;

        button.active = false;
        this.selectedCategoryButton = button;

        List<AbstractEnchantComponent> components = new ArrayList<>();
        this.applyAddEnchantsFilters(components, stackPredicate);

        this.addEnchantsLayout.clearChildren();
        this.addEnchantsLayout.children(components);
        this.updateSortButton();
    }

    private List<Enchantment> getSortedEnchantments() {
        List<Enchantment> sortedEnchantments = new ArrayList<>();

        for (var entry : Registries.ENCHANTMENT.getEntrySet()) {
            sortedEnchantments.add(entry.getValue());
        }

        sortedEnchantments.sort((element1, element2) -> Text.translatable(element1.getTranslationKey()).getString()
                .compareToIgnoreCase(Text.translatable(element2.getTranslationKey()).getString())
        );

        return sortedEnchantments;
    }

    private void setRenderButton(ButtonComponent buttonComponent, Supplier<Boolean> isEnabled, int v) {
        buttonComponent.renderer((context, button, delta) -> {
                    boolean enabled = isEnabled.get();
                    context.fill(button.x(), button.y(),
                            button.x() + button.width(), button.y() + button.height(),
                            enabled ? 0x80006000 : 0x80600000
                    );
                    context.drawTexture(FzmmIcons.TEXTURE, button.x() + 2, button.y() + 2, 48, enabled ? v : v + 16, 16, 16);
                }
        );
    }

    private void applyAddEnchantsFilters(List<AbstractEnchantComponent> enchantComponents, IEnchantPredicate categoryPredicate) {
        String search = this.searchTextBox.getText();

        for (var component : this.addEnchantsComponents) {
            Enchantment enchant = component.getEnchantment();
            boolean isDisabled = false;

            // only enchantments that are valid with the selected category are accepted
            if (!categoryPredicate.test(enchant, this.enchantBuilder.stack()))
                continue;

            // in case the option for only compatible enchantments is activated,
            // enchantments that are incompatible with the already applied ones will be displayed with strikethrough
            if (this.onlyCompatibleEnchants && !this.enchantBuilder.isCompatibleWith(enchant))
                isDisabled = true;

            // in case the option to allow duplicate enchantments is disabled,
            // no enchantments already applied will be displayed
            if (!this.allowDuplicates && this.enchantBuilder.contains(enchant))
                continue;

            // only enchantments matching the search filter are displayed
            if (!component.filter(search))
                continue;

            component.setDisabled(isDisabled);
            enchantComponents.add(component);
        }


    }

    private void applyAppliedEnchantsFilters() {
        String search = this.searchTextBox.getText();

        List<Component> appliedEnchants = new ArrayList<>();
        for (var component : this.appliedEnchantsComponents) {
            if (!component.filter(search))
                continue;

            appliedEnchants.add(component);
        }
        this.appliedEnchantsLayout.clearChildren();
        this.appliedEnchantsLayout.children(appliedEnchants);
    }

    public void updateAppliedEnchants() {
        List<EnchantmentBuilder.EnchantmentData> enchantments = this.enchantBuilder.enchantments();

        this.appliedEnchantsComponents.clear();
        for (var enchant : enchantments) {
            RemoveEnchantComponent enchantComponent = new RemoveEnchantComponent(enchant.getEnchantment(), enchant.getLevel(),
                    this.selectedCategoryButton::onPress, this, this.enchantBuilder);
            this.appliedEnchantsComponents.add(enchantComponent);
        }

        this.applyAppliedEnchantsFilters();
        this.updateSortButton();
    }

    public int getEnchantsLabelHorizontalSize() {
        return this.enchantsLabelHorizontalSize;
    }

    private void updateSortButton() {
        this.sortButton.active = this.enchantBuilder.enchantments().size() > 1;
    }

    private void addSortOverlay(FlowLayout rootComponent) {
        EnchantSortOverlay enchantSortOverlay = new EnchantSortOverlay(this, this.enchantBuilder);
        enchantSortOverlay.zIndex(300);
        rootComponent.child(enchantSortOverlay);
    }

    public boolean isOnlyCompatibleEnchants() {
        return this.onlyCompatibleEnchants;
    }

    public boolean isAllowDuplicates() {
        return this.allowDuplicates;
    }

    public FlowLayout getAddEnchantsLayout() {
        return this.addEnchantsLayout;
    }

    public FlowLayout getAppliedEnchantsLayout() {
        return this.appliedEnchantsLayout;
    }

    public ImmutableList<? extends AbstractEnchantComponent> getAddEnchantsComponents() {
        return this.addEnchantsComponents;
    }

    private interface IEnchantPredicate {
        boolean test(Enchantment enchantment, ItemStack itemStack);
    }
}
