package fzmm.zailer.me.client.gui.head_gallery;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.component.StyledItemComponent;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.head_gallery.HeadGalleryResources;
import fzmm.zailer.me.client.logic.head_gallery.MinecraftHeadsData;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.HeadUtils;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.FocusHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class HeadGalleryScreen extends BaseFzmmScreen implements IMementoScreen {

    private static final int SELECTED_TAG_COLOR = 0x43BCB2;
    private static final String TAG_BUTTON_TEXT = "fzmm.gui.headGallery.button.tags";
    private static final String TAG_LABEL_TEXT = "fzmm.gui.headGallery.label.tags-overlay";
    private static final String CATEGORY_LAYOUT_ID = "minecraft-heads-category-list";
    private static final String TAGS_BUTTON_ID = "tags-button";
    private static final String TAGS_LIST_LTR_ID = "minecraft-heads-tags-ltr";
    private static final String TAGS_OVERLAY_LABEL_ID = "tags-overlay-label";
    private static final String TAG_SEARCH_ID = "tag-search";
    private static final String CLEAR_TAGS_ID = "clear-tags";
    private static final String CLEAR_TAGS_OVERLAY_ID = "clear-tags-overlay";
    private static final String ITEM_SCALE_ID = "item-scale";
    private static final String STYLE_CHECKBOX_ID = "style-checkbox";
    private static final String CONTENT_SCROLL = "content-scroll";
    private static final String CONTENT_ID = "content";
    private static final String PAGE_PREVIOUS_BUTTON_ID = "previous-page-button";
    private static final String CURRENT_PAGE_LABEL_ID = "current-page-label";
    private static final String NEXT_PAGE_BUTTON_ID = "next-page-button";
    private static final String CONTENT_SEARCH_ID = "content-search";
    private static final String MINECRAFT_HEADS_BUTTON_ID = "minecraft-heads";
    private static final String ERROR_MESSAGE_ID = "error-message";
    private static HeadGalleryMemento memento = null;
    private int page;
    private double itemScale;
    private boolean setStyle;
    private FlowLayout contentLayout;
    private LabelComponent currentPageLabel;
    private final ObjectArrayList<MinecraftHeadsData> categoryHeads;
    private final ObjectArrayList<MinecraftHeadsData> categoryHeadsWithFilter;
    private TextBoxComponent contentSearchField;
    private ButtonComponent tagButton;
    private ButtonComponent clearTagsButton;
    private List<Component> categoryButtonList;
    private Set<String> selectedTags;
    private Set<String> availableTags;
    private LabelComponent errorLabel;
    private String selectedCategory;
    private ScrollContainer<?> contentScroll;
    private CustomHeadEntity frontEntityPreview;
    private CustomHeadEntity backEntityPreview;

    public HeadGalleryScreen(@Nullable Screen parent) {
        super("head_gallery", "headGallery", parent);
        this.categoryHeads = new ObjectArrayList<>();
        this.categoryHeadsWithFilter = new ObjectArrayList<>();
        this.setStyle = FzmmClient.CONFIG.headGallery.setStyleToHeads();
        this.itemScale = FzmmClient.CONFIG.headGallery.itemScale();
    }

    @Override
    protected void setup(FlowLayout rootComponent) {
        this.page = 1;
        this.selectedTags = new HashSet<>();
        this.availableTags = new HashSet<>();
        assert this.client != null;

        // content
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);

        this.contentScroll = rootComponent.childById(ScrollContainer.class, CONTENT_SCROLL);
        checkNull(this.contentScroll, "flow-layout", CONTENT_SCROLL);

        this.errorLabel = rootComponent.childById(LabelComponent.class, ERROR_MESSAGE_ID);
        checkNull(this.errorLabel, "label", ERROR_MESSAGE_ID);

        // content pages
        this.currentPageLabel = rootComponent.childById(LabelComponent.class, CURRENT_PAGE_LABEL_ID);
        checkNull(this.currentPageLabel, "label", CURRENT_PAGE_LABEL_ID);

        ButtonComponent previousPageButton = rootComponent.childById(ButtonComponent.class, PAGE_PREVIOUS_BUTTON_ID);
        checkNull(previousPageButton, "button", PAGE_PREVIOUS_BUTTON_ID);
        previousPageButton.onPress(buttonComponent -> this.setPage(this.page - 1));
        previousPageButton.tooltip(List.of(Text.translatable("fzmm.gui.hotkey.single"), Text.translatable("key.keyboard.left")));

        ButtonComponent nextPageButton = rootComponent.childById(ButtonComponent.class, NEXT_PAGE_BUTTON_ID);
        checkNull(nextPageButton, "button", NEXT_PAGE_BUTTON_ID);
        nextPageButton.onPress(buttonComponent -> this.setPage(this.page + 1));
        nextPageButton.tooltip(List.of(Text.translatable("fzmm.gui.hotkey.single"), Text.translatable("key.keyboard.right")));

        // categories - left options bottom
        StyledFlowLayout categoryList = rootComponent.childById(StyledFlowLayout.class, CATEGORY_LAYOUT_ID);
        checkNull(categoryList, "flow-layout", CATEGORY_LAYOUT_ID);


        this.categoryButtonList = HeadGalleryResources.CATEGORY_LIST.stream()
                .map(category -> Components.button(Text.translatable("fzmm.gui.headGallery.button.category." + category),
                                buttonComponent -> this.categoryButtonExecute(buttonComponent, category, null))
                        .renderer(FzmmStyles.DEFAULT_FLAT_BUTTON)
                        .sizing(Sizing.fill(100), Sizing.fixed(16))
                        .id(category)
                ).collect(Collectors.toList());

        categoryList.children(this.categoryButtonList)
                .surface(categoryList.styledPanel())
                .padding(Insets.of(4));


        // left options first row
        this.contentSearchField = rootComponent.childById(TextBoxComponent.class, CONTENT_SEARCH_ID);
        checkNull(this.contentSearchField, "text-box", CONTENT_SEARCH_ID);
        this.contentSearchField.onChanged().subscribe(s -> {
            this.applyFilters();
            this.setPage(this.page);
        });

        // left options second row
        this.tagButton = rootComponent.childById(ButtonComponent.class, TAGS_BUTTON_ID);
        checkNull(this.tagButton, "button", TAGS_BUTTON_ID);
        this.tagButton.setMessage(this.getTagButtonText());
        this.tagButton.onPress(this::openTagsExecute);

        this.clearTagsButton = rootComponent.childById(ButtonComponent.class, CLEAR_TAGS_ID);
        checkNull(this.clearTagsButton, "button", CLEAR_TAGS_ID);
        this.clearTagsButton.onPress(this::clearTagsExecute);

        // left options third row
        SliderWidget scaleSlider = rootComponent.childById(SliderWidget.class, ITEM_SCALE_ID);
        checkNull(scaleSlider, "number-slider", ITEM_SCALE_ID);
        //noinspection UnstableApiUsage
        scaleSlider.min(1).max(3).decimalPlaces(1).setFromDiscreteValue(this.itemScale)
                .scrollStep(1.0 / (scaleSlider.max() + scaleSlider.min())); // 0.5 step
        scaleSlider.message(s -> Text.translatable("fzmm.gui.headGallery.option.itemScale", s));
        scaleSlider.onChanged().subscribe(value -> {
            this.itemScale = value;
            this.setPage(this.page);
        });

        CheckboxComponent styleCheckbox = rootComponent.childById(CheckboxComponent.class, STYLE_CHECKBOX_ID);
        checkNull(styleCheckbox, "checkbox", STYLE_CHECKBOX_ID);
        styleCheckbox.checked(this.setStyle).onChanged(value -> {
            this.setStyle = value;
            this.setPage(this.page);
        });

        // bottom right
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(MINECRAFT_HEADS_BUTTON_ID), true, this::minecraftHeadsExecute);

        // right preview
        FlowLayout previewLayout = rootComponent.childById(FlowLayout.class, "preview-layout");
        checkNull(previewLayout, "flow-layout", "preview-layout");
        this.frontEntityPreview = new CustomHeadEntity(this.client.world);
        this.backEntityPreview = new CustomHeadEntity(this.client.world);

        EntityComponent<CustomHeadEntity> backEntityPreview = Components.entity(Sizing.fixed(48), this.backEntityPreview)
                .allowMouseRotation(true);
        backEntityPreview.onMouseDrag(0, 0, 160, 0, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        backEntityPreview.allowMouseRotation(false);

        previewLayout.child(Components.entity(Sizing.fixed(48), this.frontEntityPreview));
        previewLayout.child(backEntityPreview);
        this.updatePreview(Items.PLAYER_HEAD.getDefaultStack());

        this.applyFilters();
        this.setPage(1);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.contentSearchField, Component.FocusSource.MOUSE_CLICK);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            this.setPage(this.page - 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            this.setPage(this.page + 1);
            return true;
        }

        return false;
    }

    private void categoryButtonExecute(ButtonComponent selectedButton, String category, @Nullable Runnable callback) {
        assert this.client != null;

        for (var component : this.categoryButtonList) {
            if (component instanceof ButtonWidget button)
                button.active = false;
        }
        this.tagButton.active = false;

        HeadGalleryResources.getCategory(category).thenAccept(categoryData -> this.client.execute(() -> {
                this.client.execute(() -> {
                    this.selectedCategory = category;
                    this.categoryHeads.clear();
                    this.categoryHeads.addAll(categoryData);

                    for (var component : this.categoryButtonList) {
                        if (component instanceof ButtonWidget button)
                            button.active = true;
                    }
                    selectedButton.active = false;
                    this.tagButton.active = true;

                    this.updateAvailableTagList(categoryData);

                    if (callback == null) {
                        this.applyFilters();
                        this.setPage(1);
                    } else {
                        callback.run();
                    }
                });
        })).whenComplete((unused, throwable) -> this.client.execute(() -> {
            if (throwable == null) {
                this.errorLabel.text(Text.empty());
                return;
            }

            this.categoryHeads.clear();
            this.applyFilters();
            this.setPage(1);

            this.errorLabel.text(Text.translatable("fzmm.gui.headGallery.label.error", category)
                    .setStyle(Style.EMPTY.withColor(FzmmStyles.TEXT_ERROR_COLOR.rgb())));
            FzmmClient.LOGGER.error("[HeadGalleryScreen] Error while fetching category '{}'", category, throwable);

            for (var component : this.categoryButtonList) {
                if (component instanceof ButtonWidget button)
                    button.active = true;
            }
        }));
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
    }

    private void openTagsExecute(ButtonComponent tagButton) {
        FlowLayout tagSelectPanel = this.getModel().expandTemplate(FlowLayout.class, "select-tag", Map.of());
        tagSelectPanel.<FlowLayout>configure(layout -> {
            FlowLayout tagListLayout = layout.childById(FlowLayout.class, TAGS_LIST_LTR_ID);
            checkNull(tagListLayout, "flow-layout", TAGS_LIST_LTR_ID);

            LabelComponent tagsOverlayLabel = layout.childById(LabelComponent.class, TAGS_OVERLAY_LABEL_ID);
            checkNull(tagsOverlayLabel, "label", TAGS_OVERLAY_LABEL_ID);

            ButtonComponent clearSelectedTags = layout.childById(ButtonComponent.class, CLEAR_TAGS_OVERLAY_ID);
            checkNull(clearSelectedTags, "button", CLEAR_TAGS_OVERLAY_ID);

            tagsOverlayLabel.text(this.getTagLabelText());

            List<Component> buttonList = this.availableTags.stream()
                    .sorted()
                    .map(tag -> {
                        Text text = this.selectedTags.contains(tag) ? this.getSelectedTagText(tag) : Text.literal(tag);
                        return Components.button(text, button -> {
                            this.updateTag(button);
                            this.tagOverlayUpdateLabels(tagsOverlayLabel);
                        }).horizontalSizing(Sizing.fixed(200));
                    }).toList();

            tagListLayout.children(buttonList);

            clearSelectedTags.onPress(buttonComponent -> {
                for (var component : buttonList) {
                    if (component instanceof ButtonComponent buttonTag && this.selectedTags.contains(buttonTag.getMessage().getString()))
                        this.updateTag(buttonTag);
                }

                this.tagOverlayUpdateLabels(tagsOverlayLabel);
            });

            TextBoxComponent tagSearchBox = layout.childById(TextBoxComponent.class, TAG_SEARCH_ID);
            checkNull(tagSearchBox, "text-box", TAG_SEARCH_ID);

            tagSearchBox.onChanged().subscribe(value -> {
                List<Component> buttonListCopy = new ArrayList<>(buttonList);

                String valueToLowerCase = value.toLowerCase();
                buttonListCopy.removeIf(tagComponent -> {
                    if (!(tagComponent instanceof ButtonComponent buttonTag)) {
                        return false;
                    }

                    String message = buttonTag.getMessage().getString();
                    return !(message.toLowerCase().contains(valueToLowerCase) || this.selectedTags.contains(message));
                });

                tagListLayout.<FlowLayout>configure(flowLayout -> {
                    flowLayout.clearChildren();
                    flowLayout.children(buttonListCopy);
                });

            });
        });

        tagSelectPanel.mouseDown().subscribe((mouseX, mouseY, button) -> true);
        OverlayContainer<FlowLayout> tagOverlay = Containers.overlay(tagSelectPanel);
        tagOverlay.zIndex(500);
        this.addOverlay(tagOverlay);
    }

    private void updateTag(ButtonComponent selectedButton) {
        String value = selectedButton.getMessage().getString();
        if (this.selectedTags.contains(value)) {
            this.selectedTags.remove(value);
            selectedButton.setMessage(Text.literal(value));
        } else {
            this.selectedTags.add(value);
            selectedButton.setMessage(this.getSelectedTagText(value));
        }
    }

    private void tagOverlayUpdateLabels(LabelComponent tagsOverlayLabel) {
        this.applyFilters();
        this.setPage(this.page);

        this.tagButton.setMessage(this.getTagButtonText());
        tagsOverlayLabel.text(this.getTagLabelText());
    }

    private void clearTagsExecute(ButtonComponent button) {
        this.selectedTags.clear();
        this.tagButton.setMessage(this.getTagButtonText());

        this.applyFilters();
        this.setPage(this.page);
    }

    public void setPage(int page) {
        int maxHeadsPerPage = FzmmClient.CONFIG.headGallery.maxHeadsPerPage();
        if (page < 1)
            page = 1;

        int firstElementIndex = (page - 1) * maxHeadsPerPage;
        int lastPage = (int) Math.ceil(this.categoryHeadsWithFilter.size() / (float) maxHeadsPerPage);

        if (firstElementIndex >= this.categoryHeadsWithFilter.size()) {
            page = lastPage;
            firstElementIndex = this.categoryHeadsWithFilter.isEmpty() ? 0 : (lastPage - 1) * maxHeadsPerPage;
        }

        this.page = page;
        this.currentPageLabel.text(Text.translatable("fzmm.gui.headGallery.label.page", page, lastPage));

        int lastElementIndex = Math.min((page) * maxHeadsPerPage, this.categoryHeadsWithFilter.size());
        List<StyledItemComponent> currentPageHeads = this.getPageItems(firstElementIndex, lastElementIndex);

        assert this.client != null;

        for (var component : currentPageHeads) {
            component.mouseEnter().subscribe(() -> {
                if (this.contentScroll.isInBoundingBox(component.x(), component.y())) {
                    this.updatePreview(component.stack());
                }
            });
        }

        this.client.execute(() -> this.contentScroll.configure(component -> {
            this.contentLayout.clearChildren();
            this.contentLayout.children(currentPageHeads);
        }));
    }

    public List<StyledItemComponent> getPageItems(int startIndex, int endIndex) {
        List<StyledItemComponent> pageItems = new ArrayList<>();
        FzmmConfig config = FzmmClient.CONFIG;
        int nameColor = config.colors.headGalleryName().rgb();
        int tagsColor = config.colors.headGalleryTags().rgb();

        for (int i = startIndex; i != endIndex; i++) {
            MinecraftHeadsData minecraftHeadsData = this.categoryHeadsWithFilter.get(i);
            ItemStack head = HeadBuilder.builder()
                    .skinValue(minecraftHeadsData.value())
                    .id(minecraftHeadsData.uuid())
                    .notAddToHistory()
                    .get();


            StyledItemComponent itemComponent;
            if (this.setStyle) {
                DisplayBuilder builder = DisplayBuilder.of(head);
                builder.setName(Text.translatable("fzmm.item.headGallery.heads.name", minecraftHeadsData.name()).getString(), nameColor)
                        .addLore(Text.translatable("fzmm.item.headGallery.heads.tags.title").getString(), tagsColor);

                for (var tag : minecraftHeadsData.tags()) {
                    builder.addLore(Text.translatable("fzmm.item.headGallery.heads.tags.tag", tag).getString(), tagsColor);
                }

                itemComponent = StyledComponents.itemGive(builder.get());
            } else {
                itemComponent = StyledComponents.itemGive(head);
                itemComponent.setTooltipFromStack(false);
                itemComponent.tooltip(Text.literal(minecraftHeadsData.name()));
            }

            itemComponent.sizing(Sizing.fixed((int) (this.itemScale * 16.0d)));
            pageItems.add(itemComponent);
        }

        return pageItems;
    }

    public void applyFilters() {
        if (this.contentSearchField == null)
            return;

        this.categoryHeadsWithFilter.clear();
        this.categoryHeadsWithFilter.addAll(this.categoryHeads);

        String search = this.contentSearchField.getText().toLowerCase();
        this.categoryHeadsWithFilter.removeIf(itemComponent -> !itemComponent.filter(this.selectedTags, search));
        this.clearTagsButton.active(!this.selectedTags.isEmpty());
    }

    private void minecraftHeadsExecute(ButtonComponent button) {
        assert this.client != null;

        ConfirmLinkScreen.open(this.client.currentScreen, HeadGalleryResources.MINECRAFT_HEADS_URL);
    }

    private Text getTagButtonText() {
        return Text.translatable(TAG_BUTTON_TEXT, this.selectedTags.size());
    }

    private Text getTagLabelText() {
        return Text.translatable(TAG_LABEL_TEXT, this.selectedTags.size(), this.availableTags.size(), this.categoryHeadsWithFilter.size());
    }

    private Text getSelectedTagText(String value) {
        return Text.literal(value).setStyle(Style.EMPTY.withBold(true).withUnderline(true).withColor(SELECTED_TAG_COLOR));
    }

    private void updatePreview(ItemStack stack) {
        Optional<SkinTextures> skinTextures = HeadUtils.getSkinTextures(stack);
        if (skinTextures.isEmpty())
            return;

        this.frontEntityPreview.skin(skinTextures.get());
        this.backEntityPreview.skin(skinTextures.get());
    }


    @Override
    public void setMemento(IMementoObject memento) {
        HeadGalleryScreen.memento = (HeadGalleryMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new HeadGalleryMemento(new HashSet<>(this.selectedTags),
                this.page,
                this.selectedCategory,
                this.contentSearchField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        HeadGalleryMemento memento = (HeadGalleryMemento) mementoObject;
        this.selectedCategory = memento.category;
        this.contentSearchField.text(memento.contentSearch);

        if (memento.category != null) {
            List<Component> categoryList = new ArrayList<>(this.categoryButtonList);
            categoryList.removeIf(component -> !this.selectedCategory.equals(component.id()));
            categoryList.stream().findAny().ifPresent(component -> this.categoryButtonExecute((ButtonComponent) component, this.selectedCategory, () -> {

                this.selectedTags = memento.selectedTags;
                this.tagButton.setMessage(this.getTagButtonText());
                this.applyFilters();
                this.setPage(memento.page);
            }));
        }
    }

    private record HeadGalleryMemento(Set<String> selectedTags, int page, String category,
                                      String contentSearch) implements IMementoObject {
    }
}
