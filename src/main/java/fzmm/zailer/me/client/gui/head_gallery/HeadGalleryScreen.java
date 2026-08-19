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
import fzmm.zailer.me.client.logic.head_gallery.HeadGalleryResources;
import fzmm.zailer.me.client.logic.head_gallery.MinecraftHeadsData;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.HeadUtils;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.FocusHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class HeadGalleryScreen extends BaseFzmmScreen implements IMemento {

    private static final int SELECTED_TAG_COLOR = 0x43BCB2;
    private static final String TAG_BUTTON_TEXT = "fzmm.gui.headGallery.button.tags";
    private static final String TAG_LABEL_TEXT = "fzmm.gui.headGallery.label.tags-overlay";
    private int page;
    private double itemScale;
    private CheckboxComponent styleCheckbox;
    private FlowLayout contentLayout;
    private LabelComponent currentPageLabel;
    private final ObjectArrayList<MinecraftHeadsData> categoryHeads;
    private final ObjectArrayList<MinecraftHeadsData> categoryHeadsWithFilter;
    private TextBoxComponent contentSearchField;
    private ButtonComponent tagButton;
    private ButtonComponent clearTagsButton;
    private List<UIComponent> categoryButtonList;
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
        this.itemScale = FzmmClient.CONFIG.headGallery.itemScale();
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.page = 1;
        this.selectedTags = new HashSet<>();
        this.availableTags = new HashSet<>();

        // content
        this.contentLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "content");
        this.contentScroll = rootComponent.childByIdOrThrow(ScrollContainer.class, "content-scroll");
        this.errorLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "error-message");

        // content pages
        this.currentPageLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "current-page-label");

        ButtonComponent previousPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "previous-page-button");
        previousPageButton.onPress(buttonComponent -> this.setPage(this.page - 1));
        previousPageButton.tooltip(List.of(net.minecraft.network.chat.Component.translatable("fzmm.gui.hotkey.single"), net.minecraft.network.chat.Component.translatable("key.keyboard.left")));

        ButtonComponent nextPageButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "next-page-button");
        nextPageButton.onPress(buttonComponent -> this.setPage(this.page + 1));
        nextPageButton.tooltip(List.of(net.minecraft.network.chat.Component.translatable("fzmm.gui.hotkey.single"), net.minecraft.network.chat.Component.translatable("key.keyboard.right")));

        // categories - left options bottom
        EFlowLayout categoryList = rootComponent.childByIdOrThrow(EFlowLayout.class, "minecraft-heads-category-list");


        this.categoryButtonList = HeadGalleryResources.CATEGORY_LIST.stream()
                .map(category -> UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.gui.headGallery.button.category." + category),
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
        scaleSlider.message(s -> net.minecraft.network.chat.Component.translatable("fzmm.gui.headGallery.option.itemScale", s));
        scaleSlider.onChanged().subscribe(value -> {
            this.itemScale = value;
            this.setPage(this.page);
        });

        this.styleCheckbox = rootComponent.childByIdOrThrow(CheckboxComponent.class, "style-checkbox");
        this.styleCheckbox.checked(FzmmClient.CONFIG.headGallery.setStyleToHeads()).onChanged(value -> this.setPage(this.page));

        // bottom right
        rootComponent.childByIdOrThrow(ButtonComponent.class, "minecraft-heads-button").onPress(this::minecraftHeadsExecute);

        // right preview
        FlowLayout previewLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "preview-layout");
        this.frontEntityPreview = new CustomHeadEntity(this.minecraft.level);
        this.backEntityPreview = new CustomHeadEntity(this.minecraft.level);

        EntityComponent<CustomHeadEntity> backEntityPreview = EComponents.entity(Sizing.fixed(48), this.backEntityPreview)
                .allowMouseRotation(true);
        backEntityPreview.onMouseDrag(new MouseButtonEvent(0, 0, new MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), 160, 0);
        backEntityPreview.allowMouseRotation(false);

        previewLayout.child(EComponents.entity(Sizing.fixed(48), this.frontEntityPreview));
        previewLayout.child(backEntityPreview);

        this.applyFilters();
        this.setPage(1);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.contentSearchField, UIComponent.FocusSource.MOUSE_CLICK);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (super.keyPressed(input)) return true;

        if (input.isLeft()) {
            this.setPage(this.page - 1);
            return true;
        } else if (input.isRight()) {
            this.setPage(this.page + 1);
            return true;
        }

        return false;
    }

    private void categoryButtonExecute(ButtonComponent selectedButton, String category, @Nullable Runnable callback) {
        for (var component : this.categoryButtonList) {
            if (component instanceof Button button)
                button.active = false;
        }
        this.tagButton.active = false;

        HeadGalleryResources.getCategory(category).thenAccept(categoryData ->
                this.minecraft.execute(() -> {
                    this.selectedCategory = category;
                    this.categoryHeads.clear();
                    this.categoryHeads.addAll(categoryData);

                    for (var component : this.categoryButtonList) {
                        if (component instanceof Button button)
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
        ).whenComplete((unused, throwable) -> this.minecraft.execute(() -> {
            if (throwable == null) {
                this.errorLabel.text(net.minecraft.network.chat.Component.empty());
                return;
            }

            this.categoryHeads.clear();
            this.applyFilters();
            this.setPage(1);

            this.errorLabel.text(net.minecraft.network.chat.Component.translatable("fzmm.gui.headGallery.label.error", category)
                    .setStyle(Style.EMPTY.withColor(EStyles.TEXT_ERROR_COLOR.rgb())));
            FzmmClient.LOGGER.error("[HeadGalleryScreen] Error while fetching category '{}'", category, throwable);

            for (var component : this.categoryButtonList) {
                if (component instanceof Button button)
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

            List<UIComponent> buttonList = this.availableTags.stream()
                    .sorted()
                    .map(tag -> {
                        net.minecraft.network.chat.Component text = this.selectedTags.contains(tag) ? this.getSelectedTagText(tag) : net.minecraft.network.chat.Component.literal(tag);
                        return UIComponents.button(text, button -> {
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
                List<UIComponent> buttonListCopy = new ArrayList<>(buttonList);

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

        tagSelectPanel.mouseDown().subscribe((input, doubled) -> true);
        OverlayContainer<FlowLayout> tagOverlay = UIContainers.overlay(tagSelectPanel);
        this.addOverlay(tagOverlay);
    }

    private void updateTag(ButtonComponent selectedButton) {
        String value = selectedButton.getMessage().getString();
        if (this.selectedTags.contains(value)) {
            this.selectedTags.remove(value);
            selectedButton.setMessage(net.minecraft.network.chat.Component.literal(value));
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
        this.currentPageLabel.text(net.minecraft.network.chat.Component.translatable("fzmm.gui.headGallery.label.page", page, lastPage));

        int lastElementIndex = Math.min((page) * maxHeadsPerPage, this.categoryHeadsWithFilter.size());
        List<EItemComponent> currentPageHeads = this.getPageItems(firstElementIndex, lastElementIndex);

        for (var component : currentPageHeads) {
            component.mouseEnter().subscribe(() -> {
                if (this.contentScroll.isInBoundingBox(component.x(), component.y())) {
                    this.updatePreview(component.stack());
                }
            });
        }

        this.minecraft.execute(() -> this.contentScroll.configure(component -> {
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
            if (this.styleCheckbox.selected()) {
                DisplayBuilder builder = DisplayBuilder.of(head);
                builder.setName(net.minecraft.network.chat.Component.translatable("fzmm.item.headGallery.heads.name", minecraftHeadsData.name()).getString(), nameColor)
                        .addLore(net.minecraft.network.chat.Component.translatable("fzmm.item.headGallery.heads.tags.title").getString(), tagsColor);

                for (var tag : minecraftHeadsData.tags()) {
                    builder.addLore(net.minecraft.network.chat.Component.translatable("fzmm.item.headGallery.heads.tags.tag", tag).getString(), tagsColor);
                }

                itemComponent = EComponents.itemGive(builder.get());
            } else {
                itemComponent = EComponents.itemGive(head);
                itemComponent.setTooltipFromStack(false);
                itemComponent.tooltip(net.minecraft.network.chat.Component.literal(minecraftHeadsData.name()));
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

        String search = this.contentSearchField.getValue().toLowerCase();
        this.categoryHeadsWithFilter.removeIf(itemComponent -> !itemComponent.filter(this.selectedTags, search));
        this.clearTagsButton.active(!this.selectedTags.isEmpty());
    }

    private void minecraftHeadsExecute(ButtonComponent button) {
        assert this.minecraft.gui.screen() != null;
        ConfirmLinkScreen.confirmLinkNow(this.minecraft.gui.screen(), HeadGalleryResources.MINECRAFT_HEADS_URL, true);
    }

    private net.minecraft.network.chat.Component getTagButtonText() {
        return net.minecraft.network.chat.Component.translatable(TAG_BUTTON_TEXT, this.selectedTags.size());
    }

    private net.minecraft.network.chat.Component getTagLabelText() {
        return net.minecraft.network.chat.Component.translatable(TAG_LABEL_TEXT, this.selectedTags.size(), this.availableTags.size(), this.categoryHeadsWithFilter.size());
    }

    private net.minecraft.network.chat.Component getSelectedTagText(String value) {
        return net.minecraft.network.chat.Component.literal(value).setStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(SELECTED_TAG_COLOR));
    }

    private void updatePreview(ItemStack stack) {
        HeadUtils.getSkinTextures(stack).whenComplete((skinTextures, throwable) -> {
            if (throwable == null && skinTextures.isPresent()) {
                this.updatePreview(skinTextures.get());
            }
        });
    }

    private void updatePreview(PlayerSkin textures) {
        this.frontEntityPreview.skin(textures);
        this.backEntityPreview.skin(textures);
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.selectedCategory);
        output.writeObject(this.contentSearchField.getValue());
        output.writeBoolean(this.styleCheckbox.selected());
        output.writeObject(this.selectedTags);
        output.writeInt(this.page);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.selectedCategory = (String) input.readObject();
        this.contentSearchField.text((String) input.readObject());
        this.styleCheckbox.checked(input.readBoolean());
        if (this.selectedCategory == null) return;

        List<UIComponent> categoryList = new ArrayList<>(this.categoryButtonList);
        categoryList.removeIf(component -> !this.selectedCategory.equals(component.id()));
        categoryList.stream().findAny().ifPresent(component -> this.categoryButtonExecute((ButtonComponent) component, this.selectedCategory, () -> {

            try {
                this.selectedTags = (Set<String>) input.readObject();
                this.tagButton.setMessage(this.getTagButtonText());
                this.applyFilters();
                this.setPage(input.readInt());
            } catch (IOException | ClassNotFoundException e) {
                FzmmClient.LOGGER.error("[HeadGalleryScreen] Failed to restore category", e);
            }
        }));
    }
}
