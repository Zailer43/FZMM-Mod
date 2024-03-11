package fzmm.zailer.me.client.gui.item_editor.banner_editor;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs.BannerEditorTabs;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs.IBannerEditorTab;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class BannerEditorScreen implements IItemEditorScreen {
    private static final String BANNER_PREVIEW_ID = "banner-preview";
    private static final String COLOR_LAYOUT_ID = "color-layout";
    private static final String IS_SHIELD_ID = "isShield";
    private static final String CONTENT_ID = "content";
    private static final String UNDO_BUTTON_ID = "undo";
    private static final String REDO_BUTTON_ID = "redo";
    private static BannerEditorTabs selectedTab = BannerEditorTabs.ADD_PATTERNS;
    private ItemComponent bannerPreview;
    private BooleanButton isShieldButton;
    private BannerBuilder bannerBuilder;
    private DyeColor selectedColor;
    private ButtonComponent undoButton;
    private ButtonComponent redoButton;
    private ArrayDeque<BannerBuilder> undoArray;
    private ArrayDeque<BannerBuilder> redoArray;
    private ItemEditorBaseScreen baseScreen;
    private RequestedItem bannerRequest = null;

    public BannerEditorScreen() {
        this.bannerBuilder = BannerBuilder.of(Items.WHITE_BANNER.getDefaultStack());
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.baseScreen = baseScreen;

        //preview
        this.bannerPreview = editorLayout.childById(ItemComponent.class, BANNER_PREVIEW_ID);
        BaseFzmmScreen.checkNull(this.bannerPreview, "flow-layout", BANNER_PREVIEW_ID);

        //preview buttons
        this.undoArray = new ArrayDeque<>();
        this.undoButton = editorLayout.childById(ButtonComponent.class, UNDO_BUTTON_ID);
        BaseFzmmScreen.checkNull(this.undoButton, "button", UNDO_BUTTON_ID);
        this.undoButton.onPress(buttonComponent -> this.undo());

        this.redoArray = new ArrayDeque<>();
        this.redoButton = editorLayout.childById(ButtonComponent.class, REDO_BUTTON_ID);
        BaseFzmmScreen.checkNull(this.redoButton, "button", REDO_BUTTON_ID);
        this.redoButton.onPress(buttonComponent -> this.redo());

        this.clearUndo();

        FlowLayout contentLayout = editorLayout.childById(FlowLayout.class, CONTENT_ID);
        BaseFzmmScreen.checkNull(contentLayout, "flow-layout", CONTENT_ID);

        FlowLayout colorLayout = editorLayout.childById(FlowLayout.class, COLOR_LAYOUT_ID);
        BaseFzmmScreen.checkNull(colorLayout, "flow-layout", COLOR_LAYOUT_ID);
        List<Component> colorList = new ArrayList<>();
        DyeColor[] dyeColorsInOrder = FzmmUtils.getColorsInOrder();
        for (var dyeColor : dyeColorsInOrder) {
            BoxComponent colorBox = Components.box(Sizing.fixed(16), Sizing.fixed(16));
            colorBox.margins(Insets.of(1));
            colorBox.color(Color.ofDye(dyeColor));
            colorBox.fill(true);
            colorBox.cursorStyle(CursorStyle.HAND);

            FlowLayout colorSelectedLayout = Containers.horizontalFlow(Sizing.fixed(18), Sizing.fixed(18));
            colorSelectedLayout.padding(Insets.of(1));
            colorSelectedLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            colorBox.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.selectedColor = dyeColor;
                this.updatePreview(this.bannerBuilder, false);

                for (var component : colorList) {
                    if (component instanceof FlowLayout layout)
                        layout.surface(Surface.outline(0x00000000));
                }

                colorSelectedLayout.surface(Surface.outline(0xFFFFFFFF));

                return true;
            });

            colorSelectedLayout.child(colorBox);
            colorList.add(colorSelectedLayout);
        }

        this.selectedColor = dyeColorsInOrder[0];
        colorLayout.children(colorList);

        //tabs
        this.baseScreen.setTabs(selectedTab);
        ScreenTabRow.setup(editorLayout, "tabs", selectedTab);
        for (var bannerEditorTab : BannerEditorTabs.values()) {
            IScreenTab tab = this.baseScreen.getTab(bannerEditorTab, IBannerEditorTab.class);
            tab.setupComponents(editorLayout);
            ButtonRow.setup(editorLayout, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button -> {
                selectedTab = this.baseScreen.selectScreenTab(editorLayout, tab, selectedTab);
                this.updatePreview(this.bannerBuilder);
            });
        }
        this.baseScreen.selectScreenTab(editorLayout, selectedTab, selectedTab);

        //other
        this.isShieldButton = BooleanRow.setup(editorLayout, IS_SHIELD_ID, false, button -> {
            boolean isShield = ((BooleanButton) button).enabled();
            this.updatePreview(this.bannerBuilder.isShield(isShield));
        });
        this.isShieldButton.horizontalSizing(Sizing.fill(33));

        this.updatePreview(this.bannerBuilder);
        return editorLayout;
    }

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.bannerRequest != null)
            return List.of(this.bannerRequest);

        List<ItemStack> defaultItems = new ArrayList<>();

        for (var dye : FzmmUtils.getColorsInOrder())
            defaultItems.add(BannerBuilder.getBannerByDye(dye).getDefaultStack());

        defaultItems.add(Items.SHIELD.getDefaultStack());

        this.bannerRequest = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof ShieldItem || itemStack.getItem() instanceof BannerItem,
                this::selectItemAndUpdateParameters,
                defaultItems,
                Items.WHITE_BANNER.getDefaultStack(),
                Text.translatable("fzmm.gui.itemEditor.banner.title"),
                true
        );
        return List.of(this.bannerRequest);
    }

    public void updatePreview(BannerBuilder builder) {
        this.updatePreview(builder, true);
    }

    private void updatePreview(BannerBuilder builder, boolean canClearRedo) {
        if (canClearRedo && !this.redoArray.isEmpty())
            this.clearRedo();

        this.bannerPreview.stack(builder.get());
        this.baseScreen.getTab(selectedTab, IBannerEditorTab.class).update(this, builder, this.selectedColor);

        this.updateItemPreview();
    }

    @Override
    public void updateItemPreview() {
        this.bannerRequest.setStack(this.bannerPreview.stack());
        this.bannerRequest.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        boolean isShield = stack.getItem() instanceof ShieldItem;
        if (this.isShieldButton.enabled() != isShield)
            this.isShieldButton.onPress();

        this.bannerBuilder = BannerBuilder.of(stack);
        this.updatePreview(this.bannerBuilder);
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.WHITE_BANNER.getDefaultStack();
    }

    @Override
    public String getId() {
        return "banner";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 && (modifiers & GLFW.GLFW_MOD_SHIFT) == 0) {
            this.undo();
            return true;
        }

        if ((keyCode == GLFW.GLFW_KEY_Z && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0  && (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 )
                || (keyCode == GLFW.GLFW_KEY_Y && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0)) {

            this.redo();
            return true;
        }

        return false;
    }

    private void undo() {
        BannerBuilder currentBanner = this.bannerBuilder.copy();

        if (this.undoArray.isEmpty()) {
            if (!this.bannerBuilder.patterns().isEmpty()) {
                this.bannerBuilder.patterns().clear();
                this.updatePreview(this.bannerBuilder);
                this.redoArray.addFirst(currentBanner);
            }
            return;
        }

        BannerBuilder bannerBuilder = this.undoArray.removeFirst();
        this.bannerBuilder = bannerBuilder;
        this.updatePreview(bannerBuilder, false);
        this.redoArray.addFirst(currentBanner);

        this.redoButton.active = true;
        if (this.undoArray.isEmpty() && bannerBuilder.patterns().isEmpty())
            this.undoButton.active = false;
    }

    private void redo() {
        if (this.redoArray.isEmpty())
            return;

        BannerBuilder bannerBuilder = this.redoArray.removeFirst();
        BannerBuilder currentBanner = this.bannerBuilder.copy();
        this.bannerBuilder = bannerBuilder;
        this.updatePreview(bannerBuilder, false);
        this.undoArray.addFirst(currentBanner);

        this.undoButton.active = true;
        if (this.redoArray.isEmpty())
            this.redoButton.active = false;
    }

    public void addUndo(BannerBuilder bannerBuilder) {
        this.undoArray.addFirst(bannerBuilder.copy());
        if (this.undoArray.size() > FzmmClient.CONFIG.itemEditorBanner.maxUndo())
            this.undoArray.removeLast();

        this.redoArray.clear();

        this.redoButton.active = false;
        this.undoButton.active = true;
    }

    public void clearUndo() {
        this.undoArray.clear();
        this.undoButton.active = false;

        this.clearRedo();
    }

    public void clearRedo() {
        this.redoArray.clear();
        this.redoButton.active = false;
    }
}
