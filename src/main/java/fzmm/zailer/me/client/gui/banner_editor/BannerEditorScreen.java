package fzmm.zailer.me.client.gui.banner_editor;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.banner_editor.tabs.AddPatternTab;
import fzmm.zailer.me.client.gui.banner_editor.tabs.ChangeColorTab;
import fzmm.zailer.me.client.gui.banner_editor.tabs.IBannerTab;
import fzmm.zailer.me.client.gui.banner_editor.tabs.RemovePatternTab;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import fzmm.zailer.me.client.gui.utils.select_item.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import fzmm.zailer.me.utils.history.IClipboardState;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BannerEditorScreen extends BaseFzmmScreen {
    private static IBannerTab selectedTab = new AddPatternTab();
    private ItemComponent bannerPreview;
    private EBooleanButton isShieldButton;
    private EFlowLayout contentLayout;
    private BannerBuilder bannerBuilder;
    private DyeColor selectedColor;
    private HistoryClipboard clipboard;

    public BannerEditorScreen(@Nullable Screen parent) {
        super("banner_editor", "bannerEditor", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        //preview
        this.bannerPreview = rootComponent.childByIdOrThrow(ItemComponent.class, "banner-preview");
        this.bannerBuilder = BannerBuilder.of(Items.WHITE_BANNER.getDefaultInstance());

        //left buttons
        rootComponent.childByIdOrThrow(ButtonComponent.class, "give-button").onPress(button -> ItemUtils.give(this.bannerBuilder.get()));
        rootComponent.childByIdOrThrow(ButtonComponent.class, "select-banner-button").onPress(button -> this.selectBanner());

        EButtonComponent undoButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "undo-button");
        EButtonComponent redoButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "redo-button");
        this.clipboard = new HistoryClipboard(this.bannerBuilder, undoButton, redoButton, FzmmClient.CONFIG.itemEditorBanner.maxUndo());
        this.clipboard.onChange(this::updatePreview);

        rootComponent.childByIdOrThrow(ButtonComponent.class, "clear-button").onPress(button -> this.clearBanner());

        //content
        FlowLayout colorLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "color-layout");
        List<UIComponent> colorList = new ArrayList<>();
        DyeColor[] dyeColorsInOrder = FzmmUtils.getDyeColorsInOrder();
        for (var dyeColor : dyeColorsInOrder) {
            BoxComponent colorBox = UIComponents.box(Sizing.fixed(16), Sizing.fixed(16));
            colorBox.margins(Insets.of(1));
            colorBox.color(Color.ofDye(dyeColor));
            colorBox.fill(true);
            colorBox.cursorStyle(CursorStyle.HAND);

            FlowLayout colorSelectedLayout = EContainers.horizontalFlow(Sizing.fixed(18), Sizing.fixed(18));
            colorSelectedLayout.padding(Insets.of(1));
            colorSelectedLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            colorBox.mouseDown().subscribe((input, doubled) -> {
                this.selectedColor = dyeColor;
                this.updatePreview(this.bannerBuilder);

                for (var component : colorList) {
                    if (component instanceof FlowLayout layout)
                        layout.surface(Surface.BLANK);
                }

                colorSelectedLayout.surface(Surface.outline(0xFFFFFFFF));

                return true;
            });

            colorSelectedLayout.child(colorBox);
            colorList.add(colorSelectedLayout);
        }

        this.selectedColor = dyeColorsInOrder[0];
        colorLayout.children(colorList);

        this.contentLayout = rootComponent.childByIdOrThrow(EFlowLayout.class, "content");

        //tabs
        List<IBannerTab> tabs = List.of(new AddPatternTab(), new ChangeColorTab(), new RemovePatternTab());
        List<ButtonComponent> tabButtons = new ArrayList<>();

        for (var tab : tabs) {
            ButtonComponent button = rootComponent.childByIdOrThrow(ButtonComponent.class, tab.buttonId());
            button.active(!tab.buttonId().equals(selectedTab.buttonId()));
            tabButtons.add(button);

            button.onPress(buttonComponent -> {
                for (var tabButton : tabButtons) {
                    tabButton.active(true);
                }
                button.active(false);
                selectedTab = tab;
                this.updatePreview(this.bannerBuilder);
            });
        }

        //other
        this.isShieldButton = rootComponent.childByIdOrThrow(EBooleanButton.class, "is-shield");
        this.isShieldButton.enabled(false);
        this.isShieldButton.onPress(button -> this.isShieldButtonExecute(this.isShieldButton.enabled()));

        this.clipboard.clearUndo();
        this.updatePreview(this.bannerBuilder);
    }

    private void selectBanner() {
        List<ItemStack> defaultItems = new ArrayList<>();

        for (var dye : FzmmUtils.getDyeColorsInOrder())
            defaultItems.add(BannerBuilder.getBannerByDye(dye).getDefaultInstance());

        defaultItems.add(Items.SHIELD.getDefaultInstance());

        RequestedItem requestedItem = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof ShieldItem || itemStack.getItem() instanceof BannerItem,
                itemStack -> {
                    if (itemStack.isEmpty()) {
                        return;
                    }

                    boolean isShield = itemStack.getItem() instanceof ShieldItem;
                    if (this.isShieldButton.enabled() != isShield) {
                        this.isShieldButton.onPress();
                    }

                    this.clipboard.addUndo(this.bannerBuilder);
                    this.bannerBuilder = BannerBuilder.of(itemStack);
                    this.updatePreview(this.bannerBuilder);
                },
                defaultItems,
                this.bannerBuilder.get(),
                net.minecraft.network.chat.Component.translatable("fzmm.gui.bannerEditor.option.select.title"),
                true
        );

        assert this.minecraft != null;
        this.setScreen(new SelectItemScreen(this, requestedItem));
    }

    public void updatePreview(IClipboardState state) {
        BannerBuilder builder = (BannerBuilder) state;
        this.isShieldButton.enabledIgnoreCallback(builder.isShield());
        this.updatePreview(builder);
    }

    private void updatePreview(BannerBuilder builder) {
        this.bannerBuilder = builder;
        this.bannerPreview.stack(builder.get());
        List<UIComponent> banners = selectedTab.update(this.clipboard, builder, this.selectedColor);
        this.contentLayout.<EFlowLayout>configure(layout -> {
            layout.clearChildren();
            layout.children(banners);
        });
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.clipboard.keyPressed(input)) return true;

        return super.keyPressed(input);
    }

    private void isShieldButtonExecute(boolean value) {
        this.isShieldButton.enabledIgnoreCallback(value);
        this.updatePreview(this.bannerBuilder.isShield(value));
    }

    private void clearBanner() {
        this.clipboard.addUndo(this.bannerBuilder);
        this.updatePreview(this.bannerBuilder.clearPatterns());
    }
}
