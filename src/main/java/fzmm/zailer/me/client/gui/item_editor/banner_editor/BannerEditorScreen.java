package fzmm.zailer.me.client.gui.item_editor.banner_editor;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs.BannerEditorTabs;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs.IBannerEditorTab;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BannerEditorScreen implements IItemEditorScreen {
    private static final String BANNER_PREVIEW_ID = "banner-preview";
    private static final String COLOR_LAYOUT_ID = "color-layout";
    private static final String IS_SHIELD_ID = "isShield";
    private static final String CONTENT_ID = "content";
    private static BannerEditorTabs selectedTab = BannerEditorTabs.ADD_PATTERNS;
    private ItemComponent bannerPreview;
    private BooleanButton isShieldButton;
    private BannerBuilder bannerBuilder;
    private DyeColor selectedColor;
    private ItemEditorBaseScreen baseScreen;
    @Nullable
    private RequestedItem bannerRequest = null;

    public BannerEditorScreen() {
        this.bannerBuilder = BannerBuilder.of(Items.WHITE_BANNER.getDefaultStack());
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, int x, int y, int width, int height) {
        this.baseScreen = baseScreen;
        UIModel uiModel = BaseUIModelScreen.DataSource.asset(new Identifier(FzmmClient.MOD_ID, "item_editor/banner_editor")).get();
        if (uiModel == null) {
            FzmmClient.LOGGER.error("[BannerEditorScreen] Failed to load UIModel");
            return null;
        }

        FlowLayout rootComponent = uiModel.createAdapterWithoutScreen(x, y, width, height, FlowLayout.class).rootComponent;
        //general
        this.bannerPreview = rootComponent.childById(ItemComponent.class, BANNER_PREVIEW_ID);
        BaseFzmmScreen.checkNull(this.bannerPreview, "flow-layout", BANNER_PREVIEW_ID);

        FlowLayout contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        BaseFzmmScreen.checkNull(contentLayout, "flow-layout", CONTENT_ID);

        FlowLayout colorLayout = rootComponent.childById(FlowLayout.class, COLOR_LAYOUT_ID);
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
                this.updatePreview(this.bannerBuilder);

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
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var bannerEditorTab : BannerEditorTabs.values()) {
            IScreenTab tab = this.baseScreen.getTab(bannerEditorTab, IBannerEditorTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button -> {
                selectedTab = this.baseScreen.selectScreenTab(rootComponent, tab, selectedTab);
                this.updatePreview(this.bannerBuilder);
            });
        }
        this.baseScreen.selectScreenTab(rootComponent, selectedTab, selectedTab);

        //other
        this.isShieldButton = BooleanRow.setup(rootComponent, IS_SHIELD_ID, false, button -> {
            boolean isShield = ((BooleanButton) button).enabled();
            this.updatePreview(this.bannerBuilder.isShield(isShield));
        });
        this.isShieldButton.horizontalSizing(Sizing.fill(33));

        this.updatePreview(this.bannerBuilder);
        return rootComponent;
    }

    @Override
    public List<RequestedItem> getRequestedItems(Consumer<ItemStack> firstItemSetter) {
        if (this.bannerRequest != null)
            return List.of(this.bannerRequest);

        List<ItemStack> defaultItems = new ArrayList<>();

        AtomicBoolean isFirstAtomic = new AtomicBoolean(true);
        for (var dye : FzmmUtils.getColorsInOrder())
            defaultItems.add(BannerBuilder.getBannerByDye(dye).getDefaultStack());

        defaultItems.add(Items.SHIELD.getDefaultStack());

        this.bannerRequest = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof ShieldItem || itemStack.getItem() instanceof BannerItem,
                itemStack -> {
                    boolean isShield = itemStack.getItem() instanceof ShieldItem;
                    if (this.isShieldButton.enabled() != isShield)
                        this.isShieldButton.onPress();

                    this.bannerBuilder = BannerBuilder.of(itemStack);
                    this.updatePreview(this.bannerBuilder);

                    if (isFirstAtomic.get()) {
                        firstItemSetter.accept(this.bannerRequest.stack());
                        isFirstAtomic.set(false);
                    }
                },
                defaultItems,
                this.bannerBuilder.get(),
                Text.translatable("fzmm.gui.itemEditor.banner.title"),
                true
        );
        return List.of(this.bannerRequest);
    }

    public void updatePreview(BannerBuilder builder) {
        this.bannerPreview.stack(builder.get());
        this.baseScreen.getTab(selectedTab, IBannerEditorTab.class).update(this, builder, this.selectedColor);
        if (this.bannerRequest != null) {
            this.bannerRequest.setStack(this.bannerPreview.stack());
        }
    }

    @Override
    public void setItem(ItemStack stack) {
        boolean isShield = stack.getItem() instanceof ShieldItem;
        if (this.isShieldButton.enabled() != isShield)
            this.isShieldButton.onPress();

        this.bannerBuilder = BannerBuilder.of(stack.isEmpty() ? Items.WHITE_BANNER.getDefaultStack() : stack);
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
}
