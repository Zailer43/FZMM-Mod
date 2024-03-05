package fzmm.zailer.me.client.gui.item_editor.base;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.components.*;
import fzmm.zailer.me.client.gui.item_editor.block_state_editor.BlockStateEditor;
import fzmm.zailer.me.client.gui.item_editor.color_editor.ColorEditor;
import fzmm.zailer.me.client.gui.item_editor.container_editor.ContainerEditor;
import fzmm.zailer.me.client.gui.item_editor.effect_editor.EffectEditor;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import fzmm.zailer.me.client.gui.item_editor.filled_map_editor.FilledMapEditor;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ItemEditorBaseScreen extends BaseFzmmScreen implements ICollapsible {
    private static final String REQUIRED_ITEMS_TEXT = "fzmm.gui.itemEditor.base.label.required_items";
    private static final String APPLICABLE_EDITORS_TEXT = "fzmm.gui.itemEditor.base.label.applicable_editors";
    private static final String NON_APPLICABLE_EDITORS_TEXT = "fzmm.gui.itemEditor.base.label.non_applicable_editors";
    private static final int BASE_PANEL_WIDTH = 200;
    private static final String BASE_PANEL_ID = "base-panel";
    private static final String REQUIRED_ITEMS_ID = "required-items";
    private static final String REQUIRED_ITEMS_LABEL_LAYOUT_ID = REQUIRED_ITEMS_ID +"-label-layout";
    private static final String APPLICABLE_EDITORS_ID = "applicable-editors";
    private static final String APPLICABLE_EDITORS_LABEL_LAYOUT_ID = APPLICABLE_EDITORS_ID +"-label-layout";
    private static final String NON_APPLICABLE_EDITORS_ID = "non-applicable-editors";
    private static final String NON_APPLICABLE_EDITORS_LABEL_LAYOUT_ID = NON_APPLICABLE_EDITORS_ID +"-label-layout";
    private static final String CONTENT_ID = "content";
    private static Class<? extends IItemEditorScreen> selectedEditor = null;
    protected final List<IItemEditorScreen> itemEditorScreens;
    private FlowLayout basePanelLayout;
    private FlowLayout requiredItemsLayout;
    private FlowLayout applicableEditorsLayout;
    private FlowLayout nonApplicableEditorsLayout;
    private FlowLayout contentLayout;
    private LabelComponent requiredItemsLabel;
    private LabelComponent applicableEditorsLabel;
    private LabelComponent nonApplicableEditorsLabel;
    private IItemEditorScreen currentEditor;
    private ItemStack selectedItem;
    private BooleanButton collapseButton;

    public ItemEditorBaseScreen(@Nullable Screen parent) {
        super("item_editor/base", "itemEditor", parent);
        this.itemEditorScreens = this.getItemEditorScreens();
    }

    private List<IItemEditorScreen> getItemEditorScreens() {
        List<IItemEditorScreen> itemEditorScreens = new ArrayList<>();

        itemEditorScreens.add(new ArmorEditorScreen());
        itemEditorScreens.add(new BannerEditorScreen());
        itemEditorScreens.add(new BlockStateEditor());
        itemEditorScreens.add(new ColorEditor());
        itemEditorScreens.add(new ContainerEditor());
        itemEditorScreens.add(new EffectEditor());
        itemEditorScreens.add(new EnchantEditor());
        itemEditorScreens.add(new FilledMapEditor());

        return itemEditorScreens;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;
        this.selectedItem = this.client.player.getMainHandStack().copy();

        this.basePanelLayout = rootComponent.childById(FlowLayout.class, BASE_PANEL_ID);
        checkNull(this.basePanelLayout, "flow-layout", BASE_PANEL_ID);
        this.basePanelLayout.horizontalSizing(Sizing.fixed(BASE_PANEL_WIDTH));

        // base editor layout
        this.requiredItemsLayout = rootComponent.childById(FlowLayout.class, REQUIRED_ITEMS_ID);
        checkNull(this.requiredItemsLayout, "flow-layout", REQUIRED_ITEMS_ID);
        this.applicableEditorsLayout = rootComponent.childById(FlowLayout.class, APPLICABLE_EDITORS_ID);
        checkNull(this.applicableEditorsLayout, "flow-layout", APPLICABLE_EDITORS_ID);
        this.nonApplicableEditorsLayout = rootComponent.childById(FlowLayout.class, NON_APPLICABLE_EDITORS_ID);
        checkNull(this.nonApplicableEditorsLayout, "flow-layout", NON_APPLICABLE_EDITORS_ID);

        // labels
        FlowLayout requiredItemsLabelLayout = rootComponent.childById(FlowLayout.class, REQUIRED_ITEMS_LABEL_LAYOUT_ID);
        checkNull(requiredItemsLabelLayout, "flow-layout", REQUIRED_ITEMS_LABEL_LAYOUT_ID);
        this.requiredItemsLabel = new CollapsibleLabelComponent(Text.translatable(REQUIRED_ITEMS_TEXT),
                Text.translatable(REQUIRED_ITEMS_TEXT + ".collapsed"));
        requiredItemsLabelLayout.child(this.requiredItemsLabel);

        FlowLayout applicableEditorsLabelLayout = rootComponent.childById(FlowLayout.class, APPLICABLE_EDITORS_LABEL_LAYOUT_ID);
        checkNull(applicableEditorsLabelLayout, "flow-layout", APPLICABLE_EDITORS_LABEL_LAYOUT_ID);
        this.applicableEditorsLabel = new CollapsibleLabelComponent(Text.translatable(APPLICABLE_EDITORS_TEXT),
                Text.translatable(APPLICABLE_EDITORS_TEXT + ".collapsed"));
        applicableEditorsLabelLayout.child(this.applicableEditorsLabel);

        FlowLayout nonApplicableEditorsLabelLayout = rootComponent.childById(FlowLayout.class, NON_APPLICABLE_EDITORS_LABEL_LAYOUT_ID);
        checkNull(nonApplicableEditorsLabelLayout, "flow-layout", NON_APPLICABLE_EDITORS_LABEL_LAYOUT_ID);
        this.nonApplicableEditorsLabel = new CollapsibleLabelComponent(Text.translatable(NON_APPLICABLE_EDITORS_TEXT),
                Text.translatable(NON_APPLICABLE_EDITORS_TEXT + ".collapsed"));
        nonApplicableEditorsLabelLayout.child(this.nonApplicableEditorsLabel);

        // content
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);

        // collapse base layout
        this.collapseButton = rootComponent.childById(BooleanButton.class, "collapse-button");
        checkNull(this.collapseButton, "button", "collapse-button");
        this.collapseButton.enabled(false);

        int animationCollapsed = 32;
        Animation<Sizing> basePanelAnimation = this.basePanelLayout.horizontalSizing()
                .animate(300, Easing.CUBIC, Sizing.fixed(animationCollapsed));
        Animation<Insets> editorContentAnimation = this.contentLayout.margins()
                .animate(300, Easing.CUBIC, this.contentLayout.margins().get().withLeft(animationCollapsed + 8));
        Animation.Composed collapseAnimation = Animation.compose(basePanelAnimation, editorContentAnimation);

        this.collapseButton.onPress(buttonComponent -> {
            boolean enabled = this.collapseButton.enabled();
            if (enabled) {
                collapseAnimation.forwards();
                this.collapse();
            } else {
                collapseAnimation.backwards();
                this.expand();
            }
        });

        this.selectEditor();
    }

    @Override
    public void collapse() {
        List<Component> componentList = this.getCollapsibleComponents();
        for (var component : componentList) {
            if (component instanceof ICollapsible collapsibleComponent)
                collapsibleComponent.collapse();
        }
    }

    @Override
    public void expand() {
        List<Component> componentList = this.getCollapsibleComponents();
        for (var component : componentList) {
            if (component instanceof ICollapsible collapsibleComponent)
                collapsibleComponent.expand();
        }
    }

    private List<Component> getCollapsibleComponents() {
        ImmutableList.Builder<Component> listBuilder = ImmutableList.builder();
        listBuilder.addAll(this.applicableEditorsLayout.children());
        listBuilder.addAll(this.nonApplicableEditorsLayout.children());
        listBuilder.addAll(this.requiredItemsLayout.children());
        listBuilder.add(this.requiredItemsLabel);
        listBuilder.add(this.applicableEditorsLabel);
        listBuilder.add(this.nonApplicableEditorsLabel);
        return listBuilder.build();
    }

    private void selectEditor() {
        boolean stackEmpty = this.selectedItem.isEmpty();
        if (selectedEditor != null) {
            for (var editor : this.itemEditorScreens) {
                if (editor.getClass() == selectedEditor && editor.isApplicable(this.selectedItem) || stackEmpty) {
                    this.selectEditor(editor);
                    return;
                }
            }
        }

        for (var editor : this.itemEditorScreens) {
            if (editor.isApplicable(this.selectedItem) || stackEmpty) {
                this.selectEditor(editor);
                return;
            }
        }

        this.selectEditor(this.itemEditorScreens.get(0));
    }

    public void selectEditor(IItemEditorScreen editor) {
        selectedEditor = editor.getClass();

        // We make a copy of the selected item to prevent it from being overwritten
        // by an editor in case the editor calls editor#updateItemPreview before calling editor#selectItemAndUpdateParameters
        ItemStack selectedItemCopy = this.selectedItem.copy();

        this.currentEditor = editor;
        this.contentLayout.clearChildren();
        List<RequestedItem> requestedItems = this.currentEditor.getRequestedItems();
        Optional<FlowLayout> editorLayoutOptional = this.currentEditor.getLayoutModel(
                this.basePanelLayout.x() + this.basePanelLayout.width(),
                this.basePanelLayout.y(),
                this.width,
                this.height
        );

        boolean failedGettingLayout = false;
        try {
            FlowLayout editorLayout = editorLayoutOptional.orElseThrow();
            editorLayout = this.currentEditor.getLayout(this, editorLayout);
            this.contentLayout.child(editorLayout);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[ItemEditorBaseScreen] Failed to get editor layout", e);
            this.addErrorMessage(Text.translatable("fzmm.gui.itemEditor.label.error.editorLayout"));
            failedGettingLayout = true;
        }

        this.updateRequestedItemsComponents(requestedItems);

        if (failedGettingLayout)
            return;

        assert this.client != null;
        assert this.client.player != null;

        // In case a player has an item in his hand, but the editor assigns an item by another method
        // (like getting it from the armor), it will not be replaced, however, if the selected item is
        // not the same as the one in a player's hand, then it will be replaced so that the user does
        // not lose the item that was being edited.
        if (requestedItems.get(0).isEmpty() || !ItemStack.areEqual(selectedItemCopy, this.client.player.getMainHandStack())) {
            this.selectItemAndUpdateParameters(selectedItemCopy);
            this.selectedItem = selectedItemCopy;
        }

        editor.updateItemPreview();

        this.collapseEditorIfNeeded();
    }

    @Override
    protected void init() {
        super.init();
        // it is necessary to finish initializing the screen in order
        // to obtain the width of the components, otherwise it gives 0
        this.collapseEditorIfNeeded();
    }

    private void selectItemAndUpdateParameters(ItemStack stack) {
        try {
            this.currentEditor.selectItemAndUpdateParameters(stack);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[ItemEditorBaseScreen] Failed to select item", e);
            this.updateEditorsComponents();
            Text message = Text.translatable("fzmm.gui.itemEditor.label.error.selectItem",
                    stack.getName(), Registries.ITEM.getId(stack.getItem()).toString());

            this.addErrorMessage(message);
        }
    }

    private void addErrorMessage(Text message) {
        message = message.copy().setStyle(Style.EMPTY.withColor(0xD83F27));

        LabelComponent label = Components.label(message);
        label.horizontalSizing(Sizing.fill(100));
        FlowLayout errorLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        errorLayout.padding(Insets.of(10));
        errorLayout.surface(Surface.DARK_PANEL);
        errorLayout.child(label);

        this.contentLayout.clearChildren();
        this.contentLayout.child(errorLayout);
    }

    private void updateRequestedItemsComponents(List<RequestedItem> requestedItemList) {
        assert this.client != null;
        this.requiredItemsLayout.clearChildren();
        List<Component> componentList = new ArrayList<>();

        for (var requestedItem : requestedItemList) {
            RequestedItemComponent layout = new RequestedItemComponent(requestedItemList, requestedItem,
                    stack -> this.selectedItem = stack, this::updateEditorsComponents);

            componentList.add(layout);
        }

        this.requiredItemsLayout.children(componentList);
    }

    private void updateEditorsComponents() {
        this.applicableEditorsLabel.text(Text.translatable(APPLICABLE_EDITORS_TEXT, this.selectedItem.getItem().getName().getString()));

        this.updateEditorsComponents(this.applicableEditorsLayout, this.filterEditors(true), true);
        this.updateEditorsComponents(this.nonApplicableEditorsLayout, this.filterEditors(false), false);

        this.collapseButton.enabled(this.collapseButton.enabled());
    }

    public void updateEditorsComponents(FlowLayout layout, List<IItemEditorScreen> applicableEditors, boolean applicable) {
        assert this.client != null;
        layout.clearChildren();
        List<Component> componentList = new ArrayList<>();

        for (var applicableEditor : applicableEditors)
            componentList.add(this.getEditorRow(applicableEditor, applicable, applicableEditor == this.currentEditor));

        layout.children(componentList);
    }

    public EditorRowComponent getEditorRow(IItemEditorScreen itemEditorScreen, boolean isApplicable, boolean isSelected) {
        EditorRowComponent editorRowComponent = new EditorRowComponent(itemEditorScreen, isApplicable, isSelected);

        editorRowComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (itemEditorScreen.getClass() != selectedEditor) {
                this.selectEditor(itemEditorScreen);
                return true;
            }
            return false;
        });


        return editorRowComponent;
    }

    private List<IItemEditorScreen> filterEditors(boolean isApplicable) {
        List<IItemEditorScreen> filteredEditors = new ArrayList<>();

        for (var itemEditorScreen : this.itemEditorScreens) {
            if (itemEditorScreen.isApplicable(this.selectedItem) == isApplicable) {
                filteredEditors.add(itemEditorScreen);
            }
        }

        return filteredEditors;
    }

    private void collapseEditorIfNeeded() {
        if (this.collapseButton.enabled() || !FzmmClient.CONFIG.itemEditorBase.autoCollapseIfEditorDoesNotFit())
            return;

        AtomicBoolean result = new AtomicBoolean(false);
        this.contentLayout.forEachDescendantWhere(component -> {}, component -> {
            if (result.get())
                return false;

            if (component.width() + component.x() > this.width)
                result.set(true);

            return true;
        });

        if (result.get())
            this.collapseButton.enabled(true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.currentEditor.render(context, mouseX, mouseY, delta);
    }

    @Override
    public String getBaseScreenTranslationKey() {
        return super.getBaseScreenTranslationKey() + "." + this.currentEditor.getId();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.currentEditor.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.currentEditor.keyReleased(keyCode, scanCode, modifiers))
            return true;

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.currentEditor.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentEditor.mouseClicked(mouseX, mouseY, button))
            return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.currentEditor.mouseReleased(mouseX, mouseY, button))
            return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }
}