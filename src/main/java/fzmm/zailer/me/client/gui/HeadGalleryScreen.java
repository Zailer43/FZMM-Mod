package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.components.GiveItemComponent;
import fzmm.zailer.me.client.gui.utils.containers.VerticalItemGridLayout;
import fzmm.zailer.me.client.logic.headGallery.HeadGalleryResources;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HeadGalleryScreen extends BaseFzmmScreen {

    private static final String CATEGORY_LAYOUT_ID = "minecraft-heads-category-list";
    private static final String TAGS_LAYOUT_ID = "minecraft-heads-tags-list";
    private static final String CONTENT_ID = "content";
    private static final String PAGE_PREVIOUS_BUTTON_ID = "previous-page-button";
    private static final String CURRENT_PAGE_LABEL_ID = "current-page-label";
    private static final String NEXT_PAGE_BUTTON_ID = "next-page-button";
    private static final String CONTENT_SEARCH_ID = "content-search";
    private static final String TAGS_NBT_KEY = "tags";
    private static final String MINECRAFT_HEADS_BUTTON = "minecraft-heads";
    private int page;
    private VerticalItemGridLayout contentGridLayout;
    private LabelComponent currentPageLabel;
    private final ObjectArrayList<GiveItemComponent> heads;
    private final ObjectArrayList<GiveItemComponent> headsWithFilter;
    private TextFieldWidget contentSearchField;
    private List<Component> categoryButtonList;
    private FlowLayout tagsLayout;
    private String selectedTag;

    public HeadGalleryScreen(@Nullable Screen parent) {
        super("head_gallery", "headGallery", parent);
        this.heads = new ObjectArrayList<>();
        this.headsWithFilter = new ObjectArrayList<>();
        this.page = 1;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FlowLayout categoryList = rootComponent.childById(FlowLayout.class, CATEGORY_LAYOUT_ID);
        checkNull(categoryList, "flow-layout", CATEGORY_LAYOUT_ID);

        this.categoryButtonList = HeadGalleryResources.CATEGORY_LIST.stream()
                .map(category -> Components.button(Text.translatable("fzmm.gui.headGallery.button.category." + category), buttonComponent -> this.categoryButtonExecute(buttonComponent, category))
                        .horizontalSizing(Sizing.fill(100))
                        .margins(Insets.vertical(2))
                ).collect(Collectors.toList());

        categoryList.children(this.categoryButtonList);

        this.tagsLayout = rootComponent.childById(FlowLayout.class, TAGS_LAYOUT_ID);
        checkNull(categoryList, "flow-layout", TAGS_LAYOUT_ID);

        this.selectedTag = "";

        this.contentGridLayout = rootComponent.childById(VerticalItemGridLayout.class, CONTENT_ID);
        checkNull(this.contentGridLayout, "vertical-item-grid-layout", CONTENT_ID);

        ButtonComponent previousPageButton = rootComponent.childById(ButtonComponent.class, PAGE_PREVIOUS_BUTTON_ID);
        checkNull(previousPageButton, "button", PAGE_PREVIOUS_BUTTON_ID);
        this.currentPageLabel = rootComponent.childById(LabelComponent.class, CURRENT_PAGE_LABEL_ID);
        checkNull(this.currentPageLabel, "label", CURRENT_PAGE_LABEL_ID);
        ButtonComponent nextPageButton = rootComponent.childById(ButtonComponent.class, NEXT_PAGE_BUTTON_ID);
        checkNull(nextPageButton, "button", NEXT_PAGE_BUTTON_ID);

        previousPageButton.onPress(buttonComponent -> this.setPage(this.page - 1));
        nextPageButton.onPress(buttonComponent -> this.setPage(this.page + 1));

        this.contentSearchField = TextBoxRow.setup(rootComponent, CONTENT_SEARCH_ID, "", 255, s -> {
            this.applyFilters();
            this.setPage(this.page);
        });

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(MINECRAFT_HEADS_BUTTON), true, this::minecraftHeadsExecute);

        this.applyFilters();
        this.setPage(1);
    }

    private void categoryButtonExecute(ButtonComponent selectedButton, String category) {
        assert this.client != null;

        this.client.execute(() -> {
            for (var component : this.categoryButtonList) {
                if (component instanceof ButtonWidget button)
                    button.active = false;
            }
        });

        HeadGalleryResources.getCategory(category).thenAccept(categoryData -> this.client.execute(() -> {

            this.heads.clear();
            ObjectArrayList<GiveItemComponent> categoryHeads = categoryData.stream()
                    .map(minecraftHeadsData -> {
                        ItemStack head = HeadBuilder.builder()
                                .skinValue(minecraftHeadsData.value())
                                .id(minecraftHeadsData.uuid())
                                .notAddToHistory()
                                .get();

                        head = DisplayBuilder.of(head)
                                .setName(minecraftHeadsData.name())
                                .get();

                        NbtList tagsNbtList = new NbtList();
                        tagsNbtList.addAll(minecraftHeadsData.tags().stream()
                                .map(NbtString::of)
                                .toList()
                        );
                        head.setSubNbt(TAGS_NBT_KEY, tagsNbtList);

                        return new GiveItemComponent(head);
                    }).collect(ObjectArrayList.toList());
            this.heads.addAll(categoryHeads);

            for (var component : this.categoryButtonList) {
                if (component instanceof ButtonWidget button)
                    button.active = true;
            }
            selectedButton.active = false;

            Set<String> categoryTags = new HashSet<>();
            for (var minecraftHeadData : categoryData)
                categoryTags.addAll(minecraftHeadData.tags());

            categoryTags.removeIf(String::isBlank);

            this.tagsLayout.clearChildren();
            this.tagsLayout.children(
                    categoryTags.stream()
                            .sorted()
                            .map(tag -> Components.button(Text.literal(tag), this::tagButtonExecute)
                                    .horizontalSizing(Sizing.fill(100))
                                    .margins(Insets.vertical(2))
                            ).collect(Collectors.toList())
            );
            this.selectedTag = "";

            this.applyFilters();
            this.setPage(1);
        }));
    }

    private void tagButtonExecute(ButtonComponent selectedButton) {
        for (var child : this.tagsLayout.children()) {
            if (child instanceof ButtonWidget tagButton) {
                tagButton.setMessage(Text.literal(tagButton.getMessage().getString()));
            }
        }
        if (this.selectedTag.equals(selectedButton.getMessage().getString())) {
            this.selectedTag = "";
            selectedButton.setMessage(Text.literal(selectedButton.getMessage().getString()));
        } else {
            this.selectedTag = selectedButton.getMessage().getString();
            selectedButton.setMessage(Text.literal(this.selectedTag).setStyle(Style.EMPTY.withBold(true).withUnderline(true)));
        }

        this.applyFilters();
        this.setPage(this.page);
    }

    public void setPage(int page) {
        if (page < 1)
            page = 1;

        int firstElementIndex = (page - 1) * this.contentGridLayout.getMaxSize();
        int lastPage = (int) Math.ceil(this.headsWithFilter.size() / (float) this.contentGridLayout.getMaxSize());

        if (firstElementIndex >= this.headsWithFilter.size()) {
            page = lastPage;
            if (this.headsWithFilter.size() != 0)
                firstElementIndex = (lastPage - 1) * this.contentGridLayout.getMaxSize();
        }

        this.page = page;
        this.currentPageLabel.text(Text.translatable("fzmm.gui.headGallery.label.page", page, lastPage));

        int lastElementIndex = Math.min((page) * this.contentGridLayout.getMaxSize(), this.headsWithFilter.size());
        ObjectList<GiveItemComponent> currentPageHeads = this.headsWithFilter.subList(firstElementIndex, lastElementIndex);

        assert this.client != null;
        this.client.execute(() -> {
            this.contentGridLayout.clearChildren();
            this.contentGridLayout.children(currentPageHeads);
        });
    }

    public void applyFilters() {
        if (this.contentSearchField == null)
            return;

        this.headsWithFilter.clear();
        this.headsWithFilter.addAll(this.heads);

        String search = this.contentSearchField.getText().toLowerCase();
        this.headsWithFilter.removeIf(giveItemComponent -> {
            ItemStack stack = giveItemComponent.stack();
            Optional<String> name = DisplayBuilder.of(stack).getName();

            boolean containsSearch = name.map(s -> s.toLowerCase().contains(search)).orElse(false);
            boolean containsTags = true;
            if (containsSearch && !this.selectedTag.isBlank()) {
                NbtCompound nbt = stack.getOrCreateNbt();
                NbtList tags = nbt.getList(TAGS_NBT_KEY, NbtElement.STRING_TYPE);

                containsTags = tags.contains(NbtString.of(this.selectedTag));
            }

            return !(containsSearch && containsTags);
        });
    }

    private void minecraftHeadsExecute(ButtonComponent buttonComponent) {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(HeadGalleryResources.MINECRAFT_HEADS_URL);

            this.client.setScreen(this);
        }, HeadGalleryResources.MINECRAFT_HEADS_URL, true));
    }
}
