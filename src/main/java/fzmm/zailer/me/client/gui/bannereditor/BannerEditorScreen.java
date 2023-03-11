package fzmm.zailer.me.client.gui.bannereditor;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.containers.VerticalGridLayout;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BannerEditorScreen extends BaseFzmmScreen {
    private static final String BANNER_PREVIEW_ID = "banner-preview";
    private static final String COLOR_GRID_ID = "color-grid";
    private static final String GIVE_BUTTON_ID = "give-button";
    private static final String IS_SHIELD_ID = "isShield";
    private static final String CONTENT_ID = "content";
    private static BannerEditorTabs selectedTab = BannerEditorTabs.ADD_PATTERNS;
    private ItemComponent bannerPreview;
    private BannerBuilder bannerBuilder;
    private DyeColor selectedColor;

    public BannerEditorScreen(@Nullable Screen parent) {
        super("banner_editor", "bannerEditor", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        this.bannerPreview = rootComponent.childById(ItemComponent.class, BANNER_PREVIEW_ID);
        checkNull(this.bannerPreview, "flow-layout", BANNER_PREVIEW_ID);
        assert this.client != null;
        assert this.client.player != null;
        ItemStack mainHandStack = this.client.player.getMainHandStack();
        this.bannerBuilder = BannerBuilder.of(mainHandStack);

        ButtonComponent giveButton = rootComponent.childById(ButtonComponent.class, GIVE_BUTTON_ID);
        checkNull(giveButton, "button", GIVE_BUTTON_ID);
        giveButton.onPress(buttonComponent -> FzmmUtils.giveItem(this.bannerBuilder.get()));

        FlowLayout contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(contentLayout, "flow-layout", CONTENT_ID);

        VerticalGridLayout colorGrid = rootComponent.childById(VerticalGridLayout.class, COLOR_GRID_ID);
        checkNull(colorGrid, "vertical-grid-layout", COLOR_GRID_ID);
        List<FlowLayout> colorList = new ArrayList<>();
        DyeColor[] dyeColorsInOrder = FzmmUtils.getColorsInOrder();
        for (var dyeColor : dyeColorsInOrder) {
            BoxComponent colorBox = Components.box(Sizing.fixed(16), Sizing.fixed(16));
            colorBox.margins(Insets.of(1));
            colorBox.color(Color.ofDye(dyeColor));
            colorBox.fill(true);
            colorBox.cursorStyle(CursorStyle.HAND);

            FlowLayout colorLayout = Containers.horizontalFlow(Sizing.fixed(18), Sizing.fixed(18));
            colorLayout.padding(Insets.of(1));
            colorLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            colorBox.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.selectedColor = dyeColor;
                this.updatePreview(this.bannerBuilder);

                for (var layout : colorList) {
                    layout.surface(Surface.outline(0x00000000));
                }

                colorLayout.surface(Surface.outline(0xFFFFFFFF));

                return true;
            });

            colorLayout.child(colorBox);
            colorList.add(colorLayout);
        }

        this.selectedColor = dyeColorsInOrder[0];
        colorGrid.children(colorList);

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
        BooleanButton isShieldButton = BooleanRow.setup(rootComponent, IS_SHIELD_ID, false, button -> {
            boolean isShield = ((BooleanButton) button).enabled();
            this.updatePreview(this.bannerBuilder.isShield(isShield));
        });

        if (mainHandStack.getItem() instanceof ShieldItem) {
            isShieldButton.onPress();
        }

        this.updatePreview(this.bannerBuilder);
    }

    public void updatePreview(BannerBuilder builder) {
        this.bannerPreview.stack(builder.get());
        this.getTab(selectedTab, IBannerEditorTab.class).update(this, builder, this.selectedColor);
    }

}
