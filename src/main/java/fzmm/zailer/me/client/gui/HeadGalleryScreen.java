package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.components.GiveItemComponent;
import fzmm.zailer.me.client.gui.utils.containers.VerticalGridLayout;
import fzmm.zailer.me.client.logic.headGallery.HeadGalleryResources;
import fzmm.zailer.me.client.logic.headGallery.MinecraftHeadsData;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
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

import java.util.*;
import java.util.stream.Collectors;

public class HeadGalleryScreen extends BaseFzmmScreen {

    private static final int SELECTED_TAG_COLOR = 0x43BCB2;
    private static final String TAG_BUTTON_TEXT = "fzmm.gui.headGallery.button.tags";
    private static final String TAG_LABEL_TEXT = "fzmm.gui.headGallery.label.tags-overlay";
    private static final String CATEGORY_LAYOUT_ID = "minecraft-heads-category-list";
    private static final String TAGS_LAYOUT_ID = "tags-layout";
    private static final String TAGS_LIST_GRID_ID = "minecraft-heads-tags-grid";
    private static final String TAGS_OVERLAY_LABEL_ID = "tags-overlay-label";
    private static final String TAG_SEARCH_ID = "tag-search";
    private static final String CLEAR_SELECTED_TAGS_ID = "clear-selected-tags";
    private static final String CONTENT_ID = "content";
    private static final String PAGE_PREVIOUS_BUTTON_ID = "previous-page-button";
    private static final String CURRENT_PAGE_LABEL_ID = "current-page-label";
    private static final String NEXT_PAGE_BUTTON_ID = "next-page-button";
    private static final String CONTENT_SEARCH_ID = "content-search";
    private static final String TAGS_NBT_KEY = "tags";
    private static final String MINECRAFT_HEADS_BUTTON_ID = "minecraft-heads";
    private static final String ERROR_MESSAGE_ID = "error-message";
    private int page;
    private VerticalGridLayout contentGridLayout;
    private LabelComponent currentPageLabel;
    private final ObjectArrayList<GiveItemComponent> heads;
    private final ObjectArrayList<GiveItemComponent> headsWithFilter;
    private TextFieldWidget contentSearchField;
    private ButtonComponent tagButton;
    private List<Component> categoryButtonList;
    private Set<String> selectedTags;
    private Set<String> availableTags;
    @Nullable
    private OverlayContainer<?> tagOverlay;
    private LabelComponent errorLabel;

    public HeadGalleryScreen(@Nullable Screen parent) {
        super("head_gallery", "headGallery", parent);
        this.heads = new ObjectArrayList<>();
        this.headsWithFilter = new ObjectArrayList<>();
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.page = 1;
        this.selectedTags = new HashSet<>();
        this.availableTags = new HashSet<>();
        this.tagOverlay = null;

        FlowLayout categoryList = rootComponent.childById(FlowLayout.class, CATEGORY_LAYOUT_ID);
        checkNull(categoryList, "flow-layout", CATEGORY_LAYOUT_ID);

        this.categoryButtonList = HeadGalleryResources.CATEGORY_LIST.stream()
                .map(category -> Components.button(Text.translatable("fzmm.gui.headGallery.button.category." + category), buttonComponent -> this.categoryButtonExecute(buttonComponent, category))
                        .horizontalSizing(Sizing.fill(100))
                ).collect(Collectors.toList());

        categoryList.children(this.categoryButtonList);

        FlowLayout tagsLayout = rootComponent.childById(FlowLayout.class, TAGS_LAYOUT_ID);
        checkNull(tagsLayout, "flow-layout", TAGS_LAYOUT_ID);

        this.tagButton = Components.button(this.getTagButtonText(), buttonComponent -> this.tagButtonExecute(rootComponent));
        this.tagButton.horizontalSizing(Sizing.fill(100));

        tagsLayout.child(this.tagButton);

        this.contentGridLayout = rootComponent.childById(VerticalGridLayout.class, CONTENT_ID);
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

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(MINECRAFT_HEADS_BUTTON_ID), true, buttonComponent -> this.minecraftHeadsExecute());

        this.errorLabel = rootComponent.childById(LabelComponent.class, ERROR_MESSAGE_ID);
        checkNull(this.errorLabel, "label", ERROR_MESSAGE_ID);

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
            this.tagButton.active = false;
        });

        HeadGalleryResources.getCategory(category).thenAccept(categoryData -> this.client.execute(() -> {
            this.heads.clear();
            FzmmConfig config = FzmmClient.CONFIG;
            int nameColor = config.colors.headGalleryName().rgb();
            int tagsColor = config.colors.headGalleryTags().rgb();
            boolean stylingHeads = config.headGallery.stylingHeads();
            ObjectArrayList<GiveItemComponent> categoryHeads = categoryData.stream()
                    .map(minecraftHeadsData -> {
                        ItemStack head = HeadBuilder.builder()
                                .skinValue(minecraftHeadsData.value())
                                .id(minecraftHeadsData.uuid())
                                .notAddToHistory()
                                .get();

                        DisplayBuilder builder = DisplayBuilder.of(head);

                        if (stylingHeads) {
                            builder.setName(Text.translatable("fzmm.item.headGallery.heads.name", minecraftHeadsData.name()).getString(), nameColor)
                                    .addLore(Text.translatable("fzmm.item.headGallery.heads.tags.title").getString(), tagsColor);

                            for (var tag : minecraftHeadsData.tags())
                                builder.addLore(Text.translatable("fzmm.item.headGallery.heads.tags.tag", tag).getString(), tagsColor);

                        } else {
                            builder.setName(minecraftHeadsData.name());
                        }

                        head = builder.get();


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
            this.tagButton.active = true;

            this.updateAvailableTagList(categoryData);

            this.applyFilters();
            this.setPage(1);
        })).whenComplete((unused, throwable) -> {
            if (throwable == null) {
                this.errorLabel.text(Text.empty());
            } else {
                this.heads.clear();
                this.applyFilters();
                this.setPage(1);
                this.errorLabel.text(Text.translatable("fzmm.gui.headGallery.label.error", category, throwable.getCause())
                        .setStyle(Style.EMPTY.withColor(0xD83F27)));

                for (var component : this.categoryButtonList) {
                    if (component instanceof ButtonWidget button)
                        button.active = true;
                }
            }
        });
    }

    private void updateAvailableTagList(ObjectArrayList<MinecraftHeadsData> categoryData) {
        Set<String> categoryTags = new HashSet<>();
        for (var minecraftHeadData : categoryData)
            categoryTags.addAll(minecraftHeadData.tags());

        categoryTags.removeIf(String::isBlank);

        this.selectedTags.clear();
        this.availableTags.clear();
        this.availableTags.addAll(categoryTags);
        this.tagButton.setMessage(this.getTagButtonText());
        this.tagOverlay = null;
    }

    private void tagButtonExecute(FlowLayout rootComponent) {
        // because owo-lib does not let me modify the scroll of the scroll container
        // and I don't want to scroll back to the start
        if (this.tagOverlay == null) {
            this.tagOverlay = Containers.overlay(
                    this.getModel().expandTemplate(FlowLayout.class, "select-tag", Map.of()).<FlowLayout>configure(flowLayout -> {
                        int buttonsZIndex = 200;
                        VerticalGridLayout tagListGrid = flowLayout.childById(VerticalGridLayout.class, TAGS_LIST_GRID_ID);
                        checkNull(tagListGrid, "flow-layout", TAGS_LIST_GRID_ID);

                        LabelComponent tagsOverlayLabel = flowLayout.childById(LabelComponent.class, TAGS_OVERLAY_LABEL_ID);
                        checkNull(tagsOverlayLabel, "label", TAGS_OVERLAY_LABEL_ID);

                        ButtonComponent clearSelectedTags = flowLayout.childById(ButtonComponent.class, CLEAR_SELECTED_TAGS_ID);
                        checkNull(clearSelectedTags, "button", CLEAR_SELECTED_TAGS_ID);

                        tagsOverlayLabel.text(this.getTagLabelText());

                        List<ButtonComponent> buttonList = new ArrayList<>();
                        for (var availableTag : this.availableTags.stream().sorted().toList()) {
                            Text buttonText = this.selectedTags.contains(availableTag) ? this.getSelectedTagText(availableTag) : Text.literal(availableTag);
                            ButtonComponent button = (ButtonComponent) Components.button(buttonText, buttonComponent -> this.tagButtonExecute(buttonComponent, tagsOverlayLabel))
                                    .horizontalSizing(Sizing.fixed(200))
                                    .margins(Insets.of(4));
                            button.zIndex(buttonsZIndex);

                            buttonList.add(button);
                        }

                        tagListGrid.children(buttonList);

                        clearSelectedTags.onPress(buttonComponent -> {
                            for (var buttonTag : buttonList) {
                                if (this.selectedTags.contains(buttonTag.getMessage().getString()))
                                    buttonTag.onPress();
                            }
                        });
                        clearSelectedTags.zIndex(buttonsZIndex);

                        TextBoxRow.setup(flowLayout, TAG_SEARCH_ID, "", 100, value -> {
                            List<Component> buttonListCopy = new ArrayList<>(buttonList);

                            String valueToLowerCase = value.toLowerCase();
                            buttonListCopy.removeIf(tagComponent -> tagComponent instanceof ButtonComponent buttonTag
                                    && !buttonTag.getMessage().getString().toLowerCase().contains(valueToLowerCase));

                            tagListGrid.clearChildren();
                            tagListGrid.children(buttonListCopy);
                        });
                    }));
        }

        rootComponent.child(this.tagOverlay);
    }

    private void tagButtonExecute(ButtonComponent selectedButton, LabelComponent tagsOverlayLabel) {
        String value = selectedButton.getMessage().getString();
        if (this.selectedTags.contains(value)) {
            this.selectedTags.remove(value);
            selectedButton.setMessage(Text.literal(value));
        } else {
            this.selectedTags.add(value);
            selectedButton.setMessage(this.getSelectedTagText(value));
        }
        this.applyFilters();
        this.setPage(this.page);

        this.tagButton.setMessage(this.getTagButtonText());
        tagsOverlayLabel.text(this.getTagLabelText());
    }

    public void setPage(int page) {
        if (page < 1)
            page = 1;

        int firstElementIndex = (page - 1) * this.contentGridLayout.getMaxChildren();
        int lastPage = (int) Math.ceil(this.headsWithFilter.size() / (float) this.contentGridLayout.getMaxChildren());

        if (firstElementIndex >= this.headsWithFilter.size()) {
            page = lastPage;
            if (this.headsWithFilter.size() != 0)
                firstElementIndex = (lastPage - 1) * this.contentGridLayout.getMaxChildren();
        }

        this.page = page;
        this.currentPageLabel.text(Text.translatable("fzmm.gui.headGallery.label.page", page, lastPage));

        int lastElementIndex = Math.min((page) * this.contentGridLayout.getMaxChildren(), this.headsWithFilter.size());
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
            if (containsSearch && !this.selectedTags.isEmpty()) {
                NbtCompound nbt = stack.getOrCreateNbt();
                NbtList tags = nbt.getList(TAGS_NBT_KEY, NbtElement.STRING_TYPE);

                for (var selectedTag : this.selectedTags) {
                    containsTags = tags.contains(NbtString.of(selectedTag));

                    if (!containsTags)
                        break;
                }

            }

            return !(containsSearch && containsTags);
        });
    }

    private void minecraftHeadsExecute() {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(HeadGalleryResources.MINECRAFT_HEADS_URL);

            this.client.setScreen(this);
        }, HeadGalleryResources.MINECRAFT_HEADS_URL, true));
    }

    private Text getTagButtonText() {
        return Text.translatable(TAG_BUTTON_TEXT, this.selectedTags.size());
    }

    private Text getTagLabelText() {
        return Text.translatable(TAG_LABEL_TEXT, this.selectedTags.size(), this.headsWithFilter.size());
    }

    private Text getSelectedTagText(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withBold(true).withUnderline(true).withColor(SELECTED_TAG_COLOR));
    }
}
