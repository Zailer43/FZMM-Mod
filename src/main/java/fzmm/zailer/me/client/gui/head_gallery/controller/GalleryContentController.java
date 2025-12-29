package fzmm.zailer.me.client.gui.head_gallery.controller;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.EItemComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_gallery.components.GalleryItemDisplayOverlay;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class GalleryContentController extends AbstractGalleryContent implements IMemento {
    private final Consumer<ItemStack> previewCallback;
    protected FlowLayout contentLayout;
    protected ScrollContainer<?> contentScroll;
    protected SliderWidget scaleSlider;
    private LabelComponent currentPageLabel;
    protected int page = 1;

    public GalleryContentController(Consumer<ItemStack> updatePreviewCallback) {
        super();
        this.previewCallback = updatePreviewCallback;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void configureComponent(EFlowLayout rootComponent) {
        this.contentLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "content");
        this.contentScroll = rootComponent.childByIdOrThrow(ScrollContainer.class, "content-scroll");

        // item scale
        this.scaleSlider = rootComponent.childByIdOrThrow(SliderWidget.class, "item-scale");
        this.scaleSlider.min(1).max(3).decimalPlaces(1).setFromDiscreteValue(this.itemScale)
                .scrollStep(1.0 / (this.scaleSlider.max() + this.scaleSlider.min())); // 0.5 step
        this.scaleSlider.message(s -> Component.translatable("fzmm.gui.headGallery.option.itemScale", s));
        this.scaleSlider.onChanged().subscribe(this::itemScale);

        // style
        rootComponent.childByIdOrThrow(EButtonComponent.class, "display-button").onPress(button -> {
            if (FzmmClient.MCH_RESOURCES.heads().isEmpty()) return;

            GalleryItemDisplayOverlay displayOverlay = new GalleryItemDisplayOverlay(this.display, display -> {
                this.display = display;
                this.updatePage();
            });
            rootComponent.child(displayOverlay);
        });

        // content pages
        this.currentPageLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "current-page-label");

        ButtonComponent previousPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "previous-page-button");
        previousPageButton.onPress(buttonComponent -> this.addPage(-1));
        previousPageButton.tooltip(List.of(Component.translatable("fzmm.gui.hotkey.single"), Component.translatable("key.keyboard.left")));

        ButtonComponent nextPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "next-page-button");
        nextPageButton.onPress(buttonComponent -> this.addPage(1));
        nextPageButton.tooltip(List.of(Component.translatable("fzmm.gui.hotkey.single"), Component.translatable("key.keyboard.right")));
    }

    public void setPage(int page) {
        int maxHeadsPerPage = FzmmClient.CONFIG.headGallery.maxHeadsPerPage();
        if (page < 1) {
            page = 1;
        }

        int firstElementIndex = (page - 1) * maxHeadsPerPage;
        int lastPage = (int) Math.ceil(this.entries.size() / (float) maxHeadsPerPage);

        if (firstElementIndex >= this.entries.size()) {
            page = lastPage;
            firstElementIndex = this.entries.isEmpty() ? 0 : (lastPage - 1) * maxHeadsPerPage;
        }

        this.page = page;
        this.currentPageLabel.text(Component.translatable("fzmm.gui.headGallery.label.page", page, lastPage));

        int lastElementIndex = Math.min((page) * maxHeadsPerPage, this.entries.size());
        List<EItemComponent> pageHeads = this.getPageItems(firstElementIndex, lastElementIndex);

        for (var component : pageHeads) {
            component.mouseEnter().subscribe(() -> {
                if (this.contentScroll.isInBoundingBox(component.x(), component.y())) {
                    this.previewCallback.accept(component.stack());
                }
            });
        }

        Minecraft.getInstance().execute(() -> this.contentScroll.configure(component -> {
                this.contentLayout.clearChildren();
                this.contentLayout.children(pageHeads);
            })
        );
    }

    public List<EItemComponent> getPageItems(int startIndex, int endIndex) {
        List<EItemComponent> pageItems = new ArrayList<>(endIndex - startIndex);

        for (int i = startIndex; i != endIndex; i++) {
            pageItems.add(this.toComponent(this.entries.get(i)));//TODO: wrap with SelectableLayout (select like folders in file explorer and with right click)
        }

        return pageItems;
    }

    @Override
    public void itemScale(double value) {
        super.itemScale(value);
        this.updatePage();
    }

    @Override
    public void display(Display value) {
        super.display(value);
        this.updatePage();
    }

    @Override
    public void apply(List<MchHead> heads) {
        this.apply(heads, false);
    }

    //TODO: sorters (name, id, published at)
    public void apply(List<MchHead> heads, boolean pageReset) {
        this.entries = heads.parallelStream()
                .sorted(Comparator.comparing(MchHead::name))
                .toList();

        if (pageReset) {
            this.setPage(1);
        } else {
            this.updatePage();
        }
    }

    public void updatePage() {
        this.setPage(this.page);
    }

    public boolean addPage(int amount) {
        this.setPage(this.page + amount);
        return true;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeDouble(this.scaleSlider.value());
        output.writeObject(this.display);
        output.writeInt(this.page);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.scaleSlider.value(input.readDouble());
        this.display = (Display) input.readObject();
        this.setPage(input.readInt());
    }
}
