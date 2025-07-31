package fzmm.zailer.me.client.gui.head_gallery;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EItemComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.head_gallery.HeadGalleryResources;
import fzmm.zailer.me.client.logic.head_gallery.MinecraftHeadsData;
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
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
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
    protected void setup(EFlowLayout rootComponent) {
        this.page = 1;
        this.selectedTags = new HashSet<>();
        this.availableTags = new HashSet<>();
        assert this.client != null;

        // content
        this.contentLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "content");
        this.contentScroll = rootComponent.childByIdOrThrow(ScrollContainer.class, "content-scroll");
        this.errorLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "error-message");

        // content pages
        this.currentPageLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "current-page-label");

        ButtonComponent previousPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "previous-page-button");
        previousPageButton.onPress(buttonComponent -> this.setPage(this.page - 1));
        previousPageButton.tooltip(List.of(Text.translatable("fzmm.gui.hotkey.single"), Text.translatable("key.keyboard.left")));

        ButtonComponent nextPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "next-page-button");
        nextPageButton.onPress(buttonComponent -> this.setPage(this.page + 1));
        nextPageButton.tooltip(List.of(Text.translatable("fzmm.gui.hotkey.single"), Text.translatable("key.keyboard.right")));

        // categories - left options bottom
        EFlowLayout categoryList = rootComponent.childByIdOrThrow(EFlowLayout.class, "minecraft-heads-category-list");


        this.categoryButtonList = HeadGalleryResources.CATEGORY_LIST.stream()
                .map(category -> Components.button(Text.translatable("fzmm.gui.headGallery.button.category." + category),
                                buttonComponent -> this.categoryButtonExecute(buttonComponent, category, null))
                        .renderer(EStyles.DEFAULT_FLAT_BUTTON)
                        .sizing(Sizing.fill(100), Sizing.fixed(16))
                        .id(category)
                ).collect(Collectors.toList());

        categoryList.children(this.categoryButtonList)
                .surface(categoryList.styledPanel())
                .padding(Insets.of(4));


        // left options first row
        this.contentSearchField = rootComponent.childByIdOrThrow(TextBoxComponent.class, "content-search");
        this.contentSearchField.onChanged().subscribe(s -> {
            this.applyFilters();
            this.setPage(this.page);
        });

        // left options second row
        this.tagButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "tags-button");
        this.tagButton.setMessage(this.getTagButtonText());
        this.tagButton.onPress(this::openTagsExecute);

        this.clearTagsButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "clear-tags");
        this.clearTagsButton.onPress(this::clearTagsExecute);

        // left options third row
        SliderWidget scaleSlider = rootComponent.childByIdOrThrow(SliderWidget.class, "item-scale");
        //noinspection UnstableApiUsage
        scaleSlider.min(1).max(3).decimalPlaces(1).setFromDiscreteValue(this.itemScale)
                .scrollStep(1.0 / (scaleSlider.max() + scaleSlider.min())); // 0.5 step
        scaleSlider.message(s -> Text.translatable("fzmm.gui.headGallery.option.itemScale", s));
        scaleSlider.onChanged().subscribe(value -> {
            this.itemScale = value;
            this.setPage(this.page);
        });

        CheckboxComponent styleCheckbox = rootComponent.childByIdOrThrow(CheckboxComponent.class, "style-checkbox");
        styleCheckbox.checked(this.setStyle).onChanged(value -> {
            this.setStyle = value;
            this.setPage(this.page);
        });

        // bottom right
        rootComponent.childByIdOrThrow(ButtonComponent.class, "minecraft-heads-button").onPress(this::minecraftHeadsExecute);

        // right preview
        FlowLayout previewLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "preview-layout");
        this.frontEntityPreview = new CustomHeadEntity(this.client.world);
        this.backEntityPreview = new CustomHeadEntity(this.client.world);

        EntityComponent<CustomHeadEntity> backEntityPreview = EComponents.entity(Sizing.fixed(48), this.backEntityPreview)
                .allowMouseRotation(true);
        backEntityPreview.onMouseDrag(0, 0, 160, 0, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        backEntityPreview.allowMouseRotation(false);

        previewLayout.child(EComponents.entity(Sizing.fixed(48), this.frontEntityPreview));
        previewLayout.child(backEntityPreview);
        this.updatePreview(DefaultSkinHelper.getSteve());

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

        HeadGalleryResources.getCategory(category).thenAccept(categoryData ->
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
                })
        ).whenComplete((unused, throwable) -> this.client.execute(() -> {
            if (throwable == null) {
                this.errorLabel.text(Text.empty());
                return;
            }

            this.categoryHeads.clear();
            this.applyFilters();
            this.setPage(1);

            this.errorLabel.text(Text.translatable("fzmm.gui.headGallery.label.error", category)
                    .setStyle(Style.EMPTY.withColor(EStyles.TEXT_ERROR_COLOR.rgb())));
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
        EFlowLayout tagSelectPanel = this.getModel().expandTemplate(EFlowLayout.class, "select-tag", Map.of());
        tagSelectPanel.<EFlowLayout>configure(layout -> {
            FlowLayout tagListLayout = layout.childByIdOrThrow(FlowLayout.class, "minecraft-heads-tags-ltr");

            LabelComponent tagsOverlayLabel = layout.childByIdOrThrow(LabelComponent.class, "tags-overlay-label");
            ButtonComponent clearSelectedTags = layout.childByIdOrThrow(ButtonComponent.class, "clear-tags-overlay");

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

            TextBoxComponent tagSearchBox = layout.childByIdOrThrow(TextBoxComponent.class, "tag-search");

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
        List<EItemComponent> currentPageHeads = this.getPageItems(firstElementIndex, lastElementIndex);

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

    public List<EItemComponent> getPageItems(int startIndex, int endIndex) {
        List<EItemComponent> pageItems = new ArrayList<>();
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


            EItemComponent itemComponent;
            if (this.setStyle) {
                DisplayBuilder builder = DisplayBuilder.of(head);
                builder.setName(Text.translatable("fzmm.item.headGallery.heads.name", minecraftHeadsData.name()).getString(), nameColor)
                        .addLore(Text.translatable("fzmm.item.headGallery.heads.tags.title").getString(), tagsColor);

                for (var tag : minecraftHeadsData.tags()) {
                    builder.addLore(Text.translatable("fzmm.item.headGallery.heads.tags.tag", tag).getString(), tagsColor);
                }

                itemComponent = EComponents.itemGive(builder.get());
            } else {
                itemComponent = EComponents.itemGive(head);
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

        ConfirmLinkScreen.open(this.client.currentScreen, HeadGalleryResources.MINECRAFT_HEADS_URL, true);
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
        Optional<SkinTextures> skinTexturesOptional = HeadUtils.getSkinTextures(stack);
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);

        if (skinTexturesOptional.isPresent() || profileComponent == null) {
            this.updatePreview(skinTexturesOptional.orElse(DefaultSkinHelper.getSteve()));
            return;
        }

        assert this.client != null;
        this.client.getSkinProvider().fetchSkinTextures(profileComponent.gameProfile())
                .whenComplete((skinTextures, throwable) -> {
                    if (throwable == null && skinTextures.isPresent()) {
                        this.updatePreview(skinTextures.get());
                    }
                });
    }

    private void updatePreview(SkinTextures textures) {
        this.frontEntityPreview.skin(textures);
        this.backEntityPreview.skin(textures);
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
