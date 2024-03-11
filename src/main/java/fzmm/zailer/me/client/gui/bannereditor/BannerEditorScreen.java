package fzmm.zailer.me.client.gui.bannereditor;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.bannereditor.tabs.BannerEditorTabs;
import fzmm.zailer.me.client.gui.bannereditor.tabs.IBannerEditorTab;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.client.gui.utils.selectItem.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class BannerEditorScreen extends BaseFzmmScreen {
    private static final String BANNER_PREVIEW_ID = "banner-preview";
    private static final String COLOR_LAYOUT_ID = "color-layout";
    private static final String GIVE_BUTTON_ID = "give-button";
    private static final String SELECT_BANNER_BUTTON_ID = "select-banner-button";
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

    public BannerEditorScreen(@Nullable Screen parent) {
        super("banner_editor", "bannerEditor", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //preview
        this.bannerPreview = rootComponent.childById(ItemComponent.class, BANNER_PREVIEW_ID);
        checkNull(this.bannerPreview, "flow-layout", BANNER_PREVIEW_ID);
        this.bannerBuilder = BannerBuilder.of(Items.WHITE_BANNER.getDefaultStack());

        //preview buttons
        ButtonComponent giveButton = rootComponent.childById(ButtonComponent.class, GIVE_BUTTON_ID);
        checkNull(giveButton, "button", GIVE_BUTTON_ID);
        giveButton.onPress(buttonComponent -> FzmmUtils.giveItem(this.bannerBuilder.get()));

        ButtonComponent selectBannerButton = rootComponent.childById(ButtonComponent.class, SELECT_BANNER_BUTTON_ID);
        checkNull(selectBannerButton, "button", SELECT_BANNER_BUTTON_ID);
        selectBannerButton.onPress(buttonComponent -> this.selectBanner());

        this.undoArray = new ArrayDeque<>();
        this.undoButton = rootComponent.childById(ButtonComponent.class, UNDO_BUTTON_ID);
        checkNull(this.undoButton, "button", UNDO_BUTTON_ID);
        this.undoButton.onPress(buttonComponent -> this.undo());

        this.redoArray = new ArrayDeque<>();
        this.redoButton = rootComponent.childById(ButtonComponent.class, REDO_BUTTON_ID);
        checkNull(this.redoButton, "button", REDO_BUTTON_ID);
        this.redoButton.onPress(buttonComponent -> this.redo());

        this.clearUndo();

        //content
        FlowLayout contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(contentLayout, "flow-layout", CONTENT_ID);

        FlowLayout colorLayout = rootComponent.childById(FlowLayout.class, COLOR_LAYOUT_ID);
        checkNull(colorLayout, "flow-layout", COLOR_LAYOUT_ID);
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
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var bannerEditorTab : BannerEditorTabs.values()) {
            IScreenTab tab = this.getTab(bannerEditorTab, IBannerEditorTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button -> {
                selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab);
                this.updatePreview(this.bannerBuilder);
            });
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);

        //other
        this.isShieldButton = BooleanRow.setup(rootComponent, IS_SHIELD_ID, false, button -> {
            boolean isShield = ((BooleanButton) button).enabled();
            this.updatePreview(this.bannerBuilder.isShield(isShield));
        });

        this.clearUndo();
        this.updatePreview(this.bannerBuilder);
    }

    private void selectBanner() {
        List<ItemStack> defaultItems = new ArrayList<>();

        for (var dye : FzmmUtils.getColorsInOrder())
            defaultItems.add(BannerBuilder.getBannerByDye(dye).getDefaultStack());

        defaultItems.add(Items.SHIELD.getDefaultStack());

        RequestedItem requestedItem = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof ShieldItem || itemStack.getItem() instanceof BannerItem,
                itemStack -> {
                    boolean isShield = itemStack.getItem() instanceof ShieldItem;
                    if (this.isShieldButton.enabled() != isShield)
                        this.isShieldButton.onPress();

                    this.bannerBuilder = BannerBuilder.of(itemStack);
                    this.updatePreview(this.bannerBuilder);
                },
                defaultItems,
                this.bannerBuilder.get(),
                Text.translatable("fzmm.gui.bannerEditor.option.select.title"),
                true
        );

        assert this.client != null;
        this.client.setScreen(new SelectItemScreen(this, requestedItem));
    }

    public void updatePreview(BannerBuilder builder) {
        this.updatePreview(builder, true);
    }

    private void updatePreview(BannerBuilder builder, boolean canClearRedo) {
        if (canClearRedo && !this.redoArray.isEmpty())
            this.clearRedo();

        this.bannerPreview.stack(builder.get());
        this.getTab(selectedTab, IBannerEditorTab.class).update(this, builder, this.selectedColor);
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

        return super.keyPressed(keyCode, scanCode, modifiers);
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
