package fzmm.zailer.me.client.gui.item_editor.common.levelable;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.LevelableSortOverlay;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.BaseLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AddLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AppliedLevelableComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class LevelableEditor<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> implements IItemEditorScreen {
    private RequestedItem itemRequested = null;
    private List<RequestedItem> requestedItems = null;
    private FlowLayout appliedLevelablesLayout;
    private TextBoxComponent searchTextBox;
    protected FlowLayout addLevelablesLayout;
    private ImmutableList<AddLevelableComponent<V, D, B>> addLevelablesComponents;
    private List<AppliedLevelableComponent<V, D, B>> appliedLevelablesComponents;
    private boolean allowDuplicates;
    private boolean ignoreMaxLevel;
    protected int levelablesLabelHorizontalSize;
    protected ButtonComponent selectedCategoryButton;
    private ButtonComponent sortButton;
    protected final B levelableBuilder;

    public LevelableEditor(B builder) {
        this.levelableBuilder = builder;
    }

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.itemRequested = new RequestedItem(
                itemStack -> {
                    for (var entry : this.getEntries()) {
                        if (entry.isAcceptableItem(itemStack))
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
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.appliedLevelablesComponents = new ArrayList<>();
        this.allowDuplicates = false;
        this.ignoreMaxLevel = false;
        this.levelablesLabelHorizontalSize = 0;

        // top boolean buttons
        ButtonComponent allowDuplicatesComponent = editorLayout.childById(ButtonComponent.class, "allow-duplicates");
        BaseFzmmScreen.checkNull(allowDuplicatesComponent, "button", "allow-duplicates");
        allowDuplicatesComponent.onPress(buttonComponent -> {
            this.allowDuplicates = !this.allowDuplicates;
            this.levelableBuilder.allowDuplicates(this.allowDuplicates);
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });
        allowDuplicatesComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.levelable.option.allowDuplicates"));
        this.setBooleanButton(allowDuplicatesComponent, () -> this.allowDuplicates, 80);

        ButtonComponent ignoreMaxLevelComponent = editorLayout.childById(ButtonComponent.class, "ignore-max-level");
        BaseFzmmScreen.checkNull(ignoreMaxLevelComponent, "button", "ignore-max-level");
        ignoreMaxLevelComponent.tooltip(Text.translatable("fzmm.gui.itemEditor.levelable.option.ignoreMaxLevel"));
        this.setBooleanButton(ignoreMaxLevelComponent, () -> this.ignoreMaxLevel, 112);

        // other top buttons
        Icon sortIcon = Icon.of(Items.HOPPER);
        this.sortButton = editorLayout.childById(ButtonComponent.class, "sort-levelables");
        BaseFzmmScreen.checkNull(this.sortButton, "button", "sort-levelables");
        this.sortButton.onPress(buttonComponent -> this.addSortOverlay((FlowLayout) editorLayout.root()));
        this.sortButton.horizontalSizing(Sizing.fixed(20));
        this.sortButton.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            sortIcon.render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });


        // right top button
        ButtonComponent removeAllButton = editorLayout.childById(ButtonComponent.class, "remove-all");
        BaseFzmmScreen.checkNull(removeAllButton, "button", "remove-all");
        removeAllButton.onPress(buttonComponent -> {
            this.levelableBuilder.clear();
            this.appliedLevelablesLayout.clearChildren();
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });

        // filters
        this.searchTextBox = editorLayout.childById(TextBoxComponent.class, "search");
        BaseFzmmScreen.checkNull(this.searchTextBox, "text-box", "search");
        this.searchTextBox.onChanged().subscribe(value -> {
            this.selectedCategoryButton.onPress();
            this.updateAppliedLevelables();
        });

        ConfigTextBox setLevelsTextBox = editorLayout.childById(ConfigTextBox.class, "set-levels");
        BaseFzmmScreen.checkNull(setLevelsTextBox, "text-box", "set-levels");
        setLevelsTextBox.configureForNumber(Integer.class);
        setLevelsTextBox.setText("1");
        setLevelsTextBox.setCursorToStart(false);
        setLevelsTextBox.applyPredicate(s -> {
            try {
                int value = Integer.parseInt(s);
                return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        setLevelsTextBox.onChanged().subscribe(value -> this.setLevelsExecute(setLevelsTextBox, this.ignoreMaxLevel));


        ignoreMaxLevelComponent.onPress(buttonComponent -> {
            this.ignoreMaxLevel = !this.ignoreMaxLevel;
            this.setLevelsExecute(setLevelsTextBox, this.ignoreMaxLevel);
        });

        // content
        this.appliedLevelablesLayout = editorLayout.childById(FlowLayout.class, "applied-levelables");
        BaseFzmmScreen.checkNull(this.appliedLevelablesLayout, "flowLayout", "applied-levelables");

        // add all
        ButtonComponent addAllButton = editorLayout.childById(ButtonComponent.class, "add-all");
        BaseFzmmScreen.checkNull(addAllButton, "button", "add-all");
        addAllButton.onPress(buttonComponent -> {
            List<Component> children = this.addLevelablesLayout.children();
            for (var levelableComponent : List.copyOf(this.addLevelablesLayout.children())) {
                if (children.contains(levelableComponent))
                    ((AddLevelableComponent<?, ?, ?>) levelableComponent).addAllExecute();
            }

            this.updateItemPreview();
        });

        // categories
        FlowLayout categoriesLayout = editorLayout.childById(FlowLayout.class, "categories");
        BaseFzmmScreen.checkNull(categoriesLayout, "flowLayout", "categories");

        // add levelables layout
        this.addLevelablesLayout = editorLayout.childById(FlowLayout.class, "add-levelables-layout");
        BaseFzmmScreen.checkNull(this.addLevelablesLayout, "flowLayout", "add-levelables-layout");

        List<D> levelables = this.getSortedLevelables();

        this.setLabelSize(levelables);
        this.setupAddLevelables(levelables);
        this.updateAppliedLevelables();
        categoriesLayout.children(this.addCategories());

        return editorLayout;
    }

    @Override
    public String getUIModelId() {
        return "levelable";
    }

    private void setLevelsExecute(ConfigTextBox setLevelsTextBox, boolean ignoreMaxLevel) {
        int level = (int) setLevelsTextBox.parsedValue();
        level = MathHelper.clamp(level, Short.MIN_VALUE, Short.MAX_VALUE);
        if (!ignoreMaxLevel)
            level = this.levelableBuilder.getMaxLevel(level);

        String valueStr = String.valueOf(level);
        for (var child : this.appliedLevelablesLayout.children()) {
            if (child instanceof AppliedLevelableComponent<?, ?, ?> component) {
                component.setUpdateItemCallback(false);
                component.setLevel(valueStr);
                component.setUpdateItemCallback(true);
            }
        }
        this.updateItemPreview();
    }

    @Override
    public void updateItemPreview() {
        this.itemRequested.setStack(this.levelableBuilder.get());
        this.itemRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.levelableBuilder.stack(stack);

        this.allowDuplicates = this.levelableBuilder.allowDuplicates();
        this.ignoreMaxLevel = this.levelableBuilder.isOverMaxLevel();
        this.updateParameters(this.levelableBuilder);

        this.selectedCategoryButton.onPress();
        this.updateAppliedLevelables();
    }

    protected abstract void updateParameters(B builder);

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    private void setLabelSize(List<D> levelables) {
        int labelWidth = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        for (var levelable : levelables) {
            Text name = Text.translatable(levelable.getTranslationKey()).append("*");
            labelWidth = Math.max(labelWidth, textRenderer.getWidth(name));
        }

        this.levelablesLabelHorizontalSize = labelWidth;
    }

    private void setupAddLevelables(List<D> levelables) {
        ImmutableList.Builder<AddLevelableComponent<V, D, B>> addLevelableComponentBuilder = ImmutableList.builder();
        for (var levelable : levelables) {
            AddLevelableComponent<V, D, B> levelableComponent = this.getAddLevelableComponent(levelable, this::updateAppliedLevelables);

            addLevelableComponentBuilder.add(levelableComponent);
        }
        this.addLevelablesComponents = addLevelableComponentBuilder.build();
    }

    protected abstract List<ButtonComponent> addCategories();

    protected void addModsLevelablesToCategories(List<ButtonComponent> categoryButtonList) {
        Set<String> modsIdSet = new HashSet<>();
        for (var identifier : this.getRegistry().getIds()) {
            modsIdSet.add(identifier.getNamespace());
        }

        for (var modId : modsIdSet) {
            Text translation = modId.equals("minecraft") ?
                    Text.translatable("fzmm.gui.button.category.vanilla") :
                    Text.translatable("fzmm.gui.button.category.mod", modId);

            categoryButtonList.add(Components.button(translation, buttonComponent -> {
                ILevelablePredicate<V> predicate = (value, itemStack) -> {
                    Identifier valueId = this.getRegistry().getId(value);
                    return valueId != null && modId.equals(valueId.getNamespace());
                };
                this.applyCategory(predicate, categoryButtonList, buttonComponent);
            }));
        }
    }

    protected AppliedLevelableComponent<V, D, B> getAppliedLevelableComponent(D levelable, @Nullable Runnable callback) {
        return new AppliedLevelableComponent<>(levelable, callback, this, this.levelableBuilder);
    }

    protected AddLevelableComponent<V, D, B> getAddLevelableComponent(D levelable, @Nullable Runnable callback) {
        return new AddLevelableComponent<>(levelable, callback, this, this.levelableBuilder);
    }

    public abstract Text getLevelableName(D levelable);

    protected void applyCategory(ILevelablePredicate<V> stackPredicate, List<ButtonComponent> buttonList, ButtonComponent button) {
        for (var entry : buttonList)
            entry.active = true;

        button.active = false;
        this.selectedCategoryButton = button;

        List<BaseLevelableComponent<V, D, B>> components = new ArrayList<>();
        this.applyAddLevelablesFilters(components, stackPredicate);

        this.addLevelablesLayout.clearChildren();
        this.addLevelablesLayout.children(components);
        this.updateSortButton();
    }

    public abstract Registry<V> getRegistry();

    public abstract List<D> getEntries();

    private List<D> getSortedLevelables() {

        List<D> sortedLevelables = this.getEntries();

        sortedLevelables.sort((element1, element2) -> Text.translatable(element1.getTranslationKey()).getString()
                .compareToIgnoreCase(Text.translatable(element2.getTranslationKey()).getString())
        );

        return sortedLevelables;
    }

    protected void setBooleanButton(ButtonComponent buttonComponent, Supplier<Boolean> isEnabled, int v) {
        buttonComponent.horizontalSizing(Sizing.fixed(20));
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

    private void applyAddLevelablesFilters(List<BaseLevelableComponent<V, D, B>> levelableComponents, ILevelablePredicate<V> categoryPredicate) {
        String search = this.searchTextBox.getText();

        for (var component : this.addLevelablesComponents) {
            D levelable = component.getLevelable();
            boolean isDisabled = false;

            // only levelables that are valid with the selected category are accepted
            if (!categoryPredicate.test(levelable.getValue(), this.levelableBuilder.stack()))
                continue;

            if (this.disableFilter(levelable))
                isDisabled = true;

            // in case the option to allow duplicate levelables is disabled,
            // no levelables already applied will be displayed
            if (!this.allowDuplicates && this.levelableBuilder.contains(levelable.getValue()))
                continue;

            // only levelables matching the search filter are displayed
            if (!component.filter(search))
                continue;

            component.setDisabled(isDisabled);
            levelableComponents.add(component);
        }
    }

    protected abstract boolean disableFilter(D levelable);

    private void applyAppliedLevelablesFilters() {
        String search = this.searchTextBox.getText();

        List<Component> appliedLevelables = new ArrayList<>();
        for (var component : this.appliedLevelablesComponents) {
            if (!component.filter(search))
                continue;

            appliedLevelables.add(component);
        }
        this.appliedLevelablesLayout.clearChildren();
        this.appliedLevelablesLayout.children(appliedLevelables);
    }

    public void updateAppliedLevelables() {
        List<D> levelables = this.levelableBuilder.values();

        this.appliedLevelablesComponents.clear();
        for (var levelable : levelables) {
            AppliedLevelableComponent<V, D, B> levelableComponent = this.getAppliedLevelableComponent(levelable,
                    this.selectedCategoryButton::onPress);
            this.appliedLevelablesComponents.add(levelableComponent);
        }

        this.applyAppliedLevelablesFilters();
        this.updateSortButton();
    }

    public int getLevelablesLabelHorizontalSize() {
        return this.levelablesLabelHorizontalSize;
    }

    private void updateSortButton() {
        this.sortButton.active = this.levelableBuilder.values().size() > 1;
    }

    private void addSortOverlay(FlowLayout rootComponent) {
        LevelableSortOverlay<V, D, B> levelableSortOverlay = new LevelableSortOverlay<>(this, this.levelableBuilder);
        levelableSortOverlay.zIndex(300);
        rootComponent.child(levelableSortOverlay);
    }

    public boolean isAllowDuplicates() {
        return this.allowDuplicates;
    }

    public FlowLayout getAddLevelablesLayout() {
        return this.addLevelablesLayout;
    }

    public FlowLayout getAppliedLevelablesLayout() {
        return this.appliedLevelablesLayout;
    }

    public ImmutableList<? extends BaseLevelableComponent<V, D, B>> getAddLevelablesComponents() {
        return this.addLevelablesComponents;
    }

    protected interface ILevelablePredicate<V> {
        boolean test(V levelable, ItemStack itemStack);
    }
}
